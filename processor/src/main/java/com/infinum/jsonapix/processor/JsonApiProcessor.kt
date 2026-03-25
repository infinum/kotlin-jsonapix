package com.infinum.jsonapix.processor

import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.configurations.JsonApiXConfiguration
import com.infinum.jsonapix.processor.subprocessors.JsonApiXSubprocessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SuppressWarnings("SpreadOperator")
public class JsonApiProcessor : AbstractProcessor() {

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
        jsonApiXSubprocessor.init(JsonApiXConfiguration(processingEnv))
    }

    @Suppress("ReturnCount")
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        if (roundEnv == null) return true

        jsonApiXSubprocessor.process(roundEnv)

        return true
    }
}
