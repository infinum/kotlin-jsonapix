package com.infinum.jsonapix.processor.compile

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Proves that the KSP processor waits for types another processor generates. In the serial-name fixture,
 * Owner references the Pet model. Here Pet isn't a source file: a second processor generates it in the
 * first round, so Owner only resolves in the second round.
 */
internal class KspDeferralTest {

    @Test
    fun `models that reference types generated in a later round are generated once they resolve`(
        @TempDir workingDir: File,
    ) {
        // Golden files may be rewritten in this run, and this test doesn't know whether that has happened yet.
        assumeFalse(CompileTestSettings.updateGoldens) { "Golden files are being rewritten." }

        val fixture = Fixture.named("serial-name")
        val (laterFiles, sourceFiles) = fixture.files().partition { it.name == "Pet.kt" }
        val laterFile = laterFiles.single()

        val generated = compile(sourceFiles.map { it.toSourceFile() }, Backend.KSP, workingDir) {
            symbolProcessorProviders.add(FileGeneratingProcessorProvider(MODELS_PACKAGE, laterFile))
        }

        val laterFilePath = MODELS_PACKAGE.replace('.', '/') + "/" + laterFile.name
        assertMatchesGoldens(fixture, Backend.KSP, generated - laterFilePath)
    }

    /** Generates [file] in [packageName] in its first round, like a processor that generates a type a model uses. */
    private class FileGeneratingProcessorProvider(
        private val packageName: String,
        private val file: File,
    ) : SymbolProcessorProvider {

        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {

            private var generated = false

            override fun process(resolver: Resolver): List<KSAnnotated> {
                if (!generated) {
                    environment.codeGenerator
                        .createNewFile(Dependencies(aggregating = false), packageName, file.nameWithoutExtension)
                        .writer()
                        .use { it.write(file.readText()) }
                    generated = true
                }
                return emptyList()
            }
        }
    }

    private companion object {
        const val MODELS_PACKAGE = "com.infinum.jsonapix.data.models"
    }
}
