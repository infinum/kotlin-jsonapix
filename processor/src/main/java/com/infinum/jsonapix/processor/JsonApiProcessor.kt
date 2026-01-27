package com.infinum.jsonapix.processor

import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.configurations.JsonApiXConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXErrorConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXLinksConfiguration
import com.infinum.jsonapix.processor.configurations.JsonApiXMetaConfiguration
import com.infinum.jsonapix.processor.subprocessors.JsonApiXErrorSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXLinksSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXMetaSubprocessor
import com.infinum.jsonapix.processor.subprocessors.JsonApiXSubprocessor
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
        setOf(
            JsonApiXCollector.SUPPORTED,
            JsonApiXLinksCollector.SUPPORTED,
            JsonApiXMetaCollector.SUPPORTED,
            JsonApiXErrorCollector.SUPPORTED
        ).flatten().toMutableSet()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        jsonApiXLinksSubprocessor.init(JsonApiXLinksConfiguration(processingEnv))
        jsonApiXMetaSubprocessor.init(JsonApiXMetaConfiguration(processingEnv))
        jsonApiXErrorSubprocessor.init(JsonApiXErrorConfiguration(processingEnv))
        jsonApiXSubprocessor.init(JsonApiXConfiguration(processingEnv))
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment,
    ): Boolean {
        // Process Links, Meta, and Errors first (JsonApiX depends on their results)
        jsonApiXLinksSubprocessor.process(roundEnv)
        jsonApiXMetaSubprocessor.process(roundEnv)
        jsonApiXErrorSubprocessor.process(roundEnv)

        // Pass collected data to JsonApiX subprocessor
        jsonApiXSubprocessor.setLinksInfo(
            jsonApiXLinksSubprocessor.linksInfoMap,
            jsonApiXLinksSubprocessor.customLinksClassNames
        )
        jsonApiXSubprocessor.setMetaInfo(
            jsonApiXMetaSubprocessor.metaInfoMap,
            jsonApiXMetaSubprocessor.customMetaClassNames
        )
        jsonApiXSubprocessor.setCustomErrors(jsonApiXErrorSubprocessor.customErrors)

        // Process JsonApiX annotations
        jsonApiXSubprocessor.process(roundEnv)

        return true
    }
}
