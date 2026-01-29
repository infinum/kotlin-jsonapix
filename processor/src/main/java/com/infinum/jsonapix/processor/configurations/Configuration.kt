package com.infinum.jsonapix.processor.configurations

import java.io.File
import javax.annotation.processing.Messager
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal interface Configuration {

    fun messager(): Messager

    fun elementUtils(): Elements

    fun typeUtils(): Types
}
