package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder

internal class JsonApiXLinksValidator : Validator<JsonApiXLinksHolder> {

    // All collected links holders are valid
    override fun validate(elements: Set<JsonApiXLinksHolder>): Set<JsonApiXLinksHolder> = elements
}
