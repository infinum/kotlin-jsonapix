package com.infinum.jsonapix.processor.compile

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipFile
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationComponentRegistrar
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Compiles [sources] with [backend] and returns the generated sources, keyed by relative path.
 * The compilation has the test's classpath, without the entries that contain any of [excludedPackages],
 * like a consumer module that doesn't depend on them. The processors still have them.
 * [configure] runs after the backend is configured, so it can add to the backend's setup.
 */
internal fun compile(
    sources: List<SourceFile>,
    backend: Backend,
    workingDir: File,
    excludedPackages: Set<String> = emptySet(),
    configure: KotlinCompilation.() -> Unit = {},
): Map<String, String> {
    val compilation = KotlinCompilation().apply {
        this.workingDir = workingDir
        this.sources = sources
        if (excludedPackages.isEmpty()) {
            inheritClassPath = true
        } else {
            val hostClasspath = hostClasspath()
            classpaths = hostClasspath.without(excludedPackages)
            // A consumer's processor path has the processors' own dependencies, whatever the consumer
            // depends on. KAPT also finds its processors there, so without it, it runs none.
            processingClasspaths = hostClasspath
        }
        jvmTarget = "17"
        compilerPluginRegistrars = listOf(SerializationComponentRegistrar())
        messageOutputStream = OutputStream.nullOutputStream()
        backend.configure(this)
        configure()
    }
    val result = compilation.compile()
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode) {
        "Sources did not compile with $backend:\n${result.messages}"
    }
    return backend.generatedSourceDirs(compilation)
        .map(::readSourceTree)
        .fold(emptyMap()) { all, files -> all + files }
}

/** The test's classpath, which compilations otherwise inherit. */
private fun hostClasspath(): List<File> = System.getProperty("java.class.path").split(File.pathSeparator).map(::File)

/**
 * These classpath entries without the ones that contain any of [packages]. Fails if a package is in no
 * entry, because then leaving it out would prove nothing.
 */
private fun List<File>.without(packages: Set<String>): List<File> {
    packages.forEach { packageName ->
        check(any { it.containsPackage(packageName) }) {
            "No classpath entry contains package '$packageName', so there is nothing to leave out."
        }
    }
    return filterNot { entry -> packages.any(entry::containsPackage) }
}

/** Whether this classpath entry, a directory or a jar, contains files in [packageName] or its subpackages. */
private fun File.containsPackage(packageName: String): Boolean {
    val path = packageName.replace('.', '/') + "/"
    return when {
        isDirectory -> resolve(path).isDirectory
        extension == "jar" && isFile -> ZipFile(this).use { zip -> zip.entries().asSequence().any { it.name.startsWith(path) } }
        else -> false
    }
}
