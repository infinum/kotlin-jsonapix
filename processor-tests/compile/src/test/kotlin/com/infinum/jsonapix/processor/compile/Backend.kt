package com.infinum.jsonapix.processor.compile

import com.infinum.jsonapix.processor.JsonApiProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File

/**
 * An annotation-processing backend the compile tests run the JsonApiX processor on.
 * Every backend is compared against the same golden files.
 */
internal enum class Backend {

    KAPT {
        override fun configure(compilation: KotlinCompilation) {
            // Gradle builds run KAPT on K2, so the tests do too. kctfork would otherwise use its K1 path.
            compilation.useKapt4 = true
            compilation.annotationProcessors = listOf(JsonApiProcessor())
        }

        override fun generatedSourceDirs(compilation: KotlinCompilation): List<File> =
            listOf(compilation.kaptKotlinGeneratedDir, compilation.kaptSourceDir)
    },
    ;

    /** Registers this backend's processor on [compilation]. */
    abstract fun configure(compilation: KotlinCompilation)

    /** The directories this backend writes generated sources into while [compilation] runs. */
    abstract fun generatedSourceDirs(compilation: KotlinCompilation): List<File>
}
