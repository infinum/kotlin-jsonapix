package com.infinum.jsonapix.processor

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.configurations.JsonApiXConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXErrorConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXLinksConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXMetaConfiguration
import com.infinum.jsonapix.processor.specs.generators.JsonApiXMainSpecGenerator
import com.infinum.jsonapix.processor.subprocessors.JsonApiXErrorSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXLinksSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXMetaSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXSubprocessor
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SuppressWarnings("SpreadOperator")
public class JsonApiProcessor : AbstractProcessor() {

    private val jsonApiXLinksSubprocessor = JsonApiXLinksSubprocessor()
    private val jsonApiXMetaSubprocessor = JsonApiXMetaSubprocessor()
    private val jsonApiXErrorSubprocessor = JsonApiXErrorSubprocessor()
    private val jsonApiXSubprocessor = JsonApiXSubprocessor()

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(
            JsonApiXCollector.SUPPORTED,
            JsonApiXLinksCollector.SUPPORTED,
            JsonApiXMetaCollector.SUPPORTED,
            JsonApiXErrorCollector.SUPPORTED,
        ).flatten().toMutableSet()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        jsonApiXLinksSubprocessor.init(JsonApiXLinksConfiguration(processingEnv))
        jsonApiXMetaSubprocessor.init(JsonApiXMetaConfiguration(processingEnv))
        jsonApiXErrorSubprocessor.init(JsonApiXErrorConfiguration(processingEnv))
        jsonApiXSubprocessor.init(JsonApiXConfiguration(processingEnv))
    }

    @Suppress("ReturnCount")
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        if (roundEnv == null) return true

        // Collect holders from all subprocessors
        val linksHolders = jsonApiXLinksSubprocessor.process(roundEnv)
        val metaHolders = jsonApiXMetaSubprocessor.process(roundEnv)
        val errorHolders = jsonApiXErrorSubprocessor.process(roundEnv)
        val holders = jsonApiXSubprocessor.process(roundEnv)

        // Only generate if we have JsonApiX annotated classes
        if (holders.isEmpty()) return true

        // Get output directory
        val outputDir = processingEnv.options[JsonApiConstants.KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?.let(::File) ?: return true

        // Generate all specs via MainSpecGenerator
        JsonApiXMainSpecGenerator(
            outputDir = outputDir,
            holders = holders,
            linksHolders = linksHolders,
            metaHolders = metaHolders,
            errorHolders = errorHolders,
        ).generate()

        return true
    }
}
