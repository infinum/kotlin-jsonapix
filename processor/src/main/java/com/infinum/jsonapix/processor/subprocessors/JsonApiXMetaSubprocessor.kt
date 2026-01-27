package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXMetaSubprocessor : CommonSubprocessor<Set<JsonApiXMetaHolder>>() {

    override fun process(roundEnvironment: RoundEnvironment): Set<JsonApiXMetaHolder> {
        val collector = JsonApiXMetaCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXMetaValidator()

        val holders = collector.collect()
        return validator.validate(holders)
    }
}
