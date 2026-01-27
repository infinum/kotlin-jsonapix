package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder

internal class JsonApiXErrorValidator : Validator<JsonApiXErrorHolder> {

    override fun validate(elements: Set<JsonApiXErrorHolder>): Set<JsonApiXErrorHolder> {
        // All collected error holders are valid
        return elements
    }
}
