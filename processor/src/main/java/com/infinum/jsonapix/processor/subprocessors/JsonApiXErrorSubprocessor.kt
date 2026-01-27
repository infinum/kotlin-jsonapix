package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXErrorSubprocessor : CommonSubprocessor() {

    private val _customErrors = mutableMapOf<String, ClassName>()

    val customErrors: Map<String, ClassName>
        get() = _customErrors

    override fun process(roundEnvironment: RoundEnvironment) {
        val collector = JsonApiXErrorCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXErrorValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        validatedHolders.forEach { holder ->
            processErrorHolder(holder)
        }
    }

    private fun processErrorHolder(holder: JsonApiXErrorHolder) {
        _customErrors[holder.type] = holder.className
    }
}
