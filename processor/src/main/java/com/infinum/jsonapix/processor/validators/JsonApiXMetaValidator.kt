package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder

internal class JsonApiXMetaValidator : Validator<JsonApiXMetaHolder> {

    // All collected meta holders are valid
    override fun validate(elements: Set<JsonApiXMetaHolder>): Set<JsonApiXMetaHolder> = elements
}
