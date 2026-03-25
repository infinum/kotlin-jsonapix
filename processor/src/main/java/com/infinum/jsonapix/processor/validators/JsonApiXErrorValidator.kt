package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder

internal class JsonApiXErrorValidator : Validator<JsonApiXErrorHolder> {

    // All collected error holders are valid
    override fun validate(elements: Set<JsonApiXErrorHolder>): Set<JsonApiXErrorHolder> = elements
}
