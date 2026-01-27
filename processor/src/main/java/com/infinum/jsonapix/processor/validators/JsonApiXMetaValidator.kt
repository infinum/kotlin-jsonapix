package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder

internal class JsonApiXMetaValidator : Validator<JsonApiXMetaHolder> {

    override fun validate(elements: Set<JsonApiXMetaHolder>): Set<JsonApiXMetaHolder> {
        // All collected meta holders are valid
        return elements
    }
}
