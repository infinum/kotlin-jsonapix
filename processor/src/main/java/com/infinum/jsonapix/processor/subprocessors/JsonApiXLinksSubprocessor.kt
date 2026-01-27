package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXLinksSubprocessor : CommonSubprocessor<Set<JsonApiXLinksHolder>>() {

    override fun process(roundEnvironment: RoundEnvironment): Set<JsonApiXLinksHolder> {
        val collector = JsonApiXLinksCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXLinksValidator()

        val holders = collector.collect()
        return validator.validate(holders)
    }
}
