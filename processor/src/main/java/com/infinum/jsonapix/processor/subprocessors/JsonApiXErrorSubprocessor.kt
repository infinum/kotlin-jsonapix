package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXErrorSubprocessor : CommonSubprocessor<Set<JsonApiXErrorHolder>>() {

    override fun process(roundEnvironment: RoundEnvironment): Set<JsonApiXErrorHolder> {
        val collector = JsonApiXErrorCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXErrorValidator()

        val holders = collector.collect()
        return validator.validate(holders)
    }
}
