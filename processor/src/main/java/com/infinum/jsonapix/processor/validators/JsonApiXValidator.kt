package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.ElementKind
import javax.tools.Diagnostic

internal class JsonApiXValidator(
    private val messager: Messager
) : Validator<JsonApiXHolder> {

    override fun validate(elements: Set<JsonApiXHolder>): Set<JsonApiXHolder> {
        // All holders collected are already filtered to be classes
        // Additional validation can be added here if needed
        return elements
    }
}
