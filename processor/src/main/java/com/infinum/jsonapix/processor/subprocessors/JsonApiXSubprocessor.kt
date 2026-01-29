package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.validators.JsonApiXValidator
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXSubprocessor : CommonSubprocessor<Set<JsonApiXHolder>>() {

    override fun process(roundEnvironment: RoundEnvironment): Set<JsonApiXHolder> {
        val collector = JsonApiXCollector(
            roundEnvironment = roundEnvironment,
            elementUtils = elementUtils,
            typeUtils = typeUtils,
        )
        val validator = JsonApiXValidator()

        val holders = collector.collect()
        return validator.validate(holders)
    }
}
