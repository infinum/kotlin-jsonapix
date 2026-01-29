package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXHolder

internal class JsonApiXValidator : Validator<JsonApiXHolder> {

    // All holders collected are already filtered to be classes
    // Additional validation can be added here if needed
    override fun validate(elements: Set<JsonApiXHolder>): Set<JsonApiXHolder> = elements
}
