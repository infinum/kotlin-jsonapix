package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.configurations.Configuration
import javax.annotation.processing.RoundEnvironment

internal interface Subprocessor {

    fun init(configuration: Configuration)

    fun process(roundEnvironment: RoundEnvironment)
}
