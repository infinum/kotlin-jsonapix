package com.infinum.jsonapix.processor.compile

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import java.io.File
import java.io.OutputStream
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationComponentRegistrar
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Compiles [sources] with [backend] and returns the generated sources, keyed by relative path.
 * [configure] runs after the backend is configured, so it can add to the backend's setup.
 */
internal fun compile(
    sources: List<SourceFile>,
    backend: Backend,
    workingDir: File,
    configure: KotlinCompilation.() -> Unit = {},
): Map<String, String> {
    val compilation = KotlinCompilation().apply {
        this.workingDir = workingDir
        this.sources = sources
        inheritClassPath = true
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
