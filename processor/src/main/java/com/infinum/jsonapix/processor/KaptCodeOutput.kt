package com.infinum.jsonapix.processor

import com.infinum.jsonapix.processor.generation.CodeOutput
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.Filer
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

/**
 * Writes through the Filer, attaching each originating model's element, so that KAPT's incremental
 * processing knows which models a file came from. The Filer puts the files into KAPT's source output
 * folder, which KAPT compiles along with the consumer's sources. The processor is registered as
 * aggregating as a whole, so KAPT has no use for a file's aggregating flag.
 */
internal class KaptCodeOutput(
    private val filer: Filer,
    private val elementUtils: Elements,
) : CodeOutput {

    @Suppress("SpreadOperator")
    override fun write(fileSpec: FileSpec, originatingModels: Set<ClassName>, aggregating: Boolean) {
        val originatingElements = originatingModels.map { model ->
            checkNotNull(elementUtils.getTypeElement(model.canonicalName)) { "No element found for model $model." }
        }
        val file = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            fileSpec.packageName,
            fileSpec.relativePath.substringAfterLast('/'),
            *originatingElements.toTypedArray(),
        )
        file.openWriter().use { writer -> fileSpec.writeTo(writer) }
    }
}
