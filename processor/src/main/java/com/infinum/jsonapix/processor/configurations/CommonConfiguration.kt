package com.infinum.jsonapix.processor.configurations

import com.infinum.jsonapix.core.common.JsonApiConstants
import java.io.File
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal abstract class CommonConfiguration(
    private val processingEnv: ProcessingEnvironment
) : Configuration {

    override fun messager(): Messager = processingEnv.messager

    override fun outputDir(): File? =
        processingEnv.options[JsonApiConstants.KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let(::File)

    override fun elementUtils(): Elements = processingEnv.elementUtils

    override fun typeUtils(): Types = processingEnv.typeUtils
}
