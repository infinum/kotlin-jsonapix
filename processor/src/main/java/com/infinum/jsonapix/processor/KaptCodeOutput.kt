package com.infinum.jsonapix.processor

import com.infinum.jsonapix.processor.generation.CodeOutput
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import java.io.File

/**
 * Writes straight into the `kapt.kotlin.generated` folder. Writing directly bypasses the Filer,
 * so the originating models have nowhere to go and are ignored.
 */
internal class KaptCodeOutput(
    private val outputDir: File,
) : CodeOutput {

    override fun write(fileSpec: FileSpec, originatingModels: Set<ClassName>) {
        fileSpec.writeTo(outputDir)
    }
}
