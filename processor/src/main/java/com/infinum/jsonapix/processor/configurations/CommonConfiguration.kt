package com.infinum.jsonapix.processor.configurations

import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal abstract class CommonConfiguration(
    private val processingEnv: ProcessingEnvironment,
) : Configuration {

    override fun messager(): Messager = processingEnv.messager

    override fun elementUtils(): Elements = processingEnv.elementUtils

    override fun typeUtils(): Types = processingEnv.typeUtils
}
