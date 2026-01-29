package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.configurations.Configuration
import javax.annotation.processing.Messager
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@Suppress("LateinitUsage")
internal abstract class CommonSubprocessor<R> : Subprocessor<R> {

    protected lateinit var messager: Messager
    protected lateinit var elementUtils: Elements
    protected lateinit var typeUtils: Types

    override fun init(configuration: Configuration) {
        messager = configuration.messager()
        elementUtils = configuration.elementUtils()
        typeUtils = configuration.typeUtils()
    }
}
