package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.models.JsonApiXErrorResult
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXErrorSubprocessor : CommonSubprocessor<JsonApiXErrorResult>() {

    override fun process(roundEnvironment: RoundEnvironment): JsonApiXErrorResult {
        val collector = JsonApiXErrorCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXErrorValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        if (validatedHolders.isEmpty()) {
            return JsonApiXErrorResult.EMPTY
        }

        val customErrors = mutableMapOf<String, ClassName>()

        validatedHolders.forEach { holder ->
            customErrors[holder.type] = holder.className
        }

        return JsonApiXErrorResult(customErrors)
    }
}
