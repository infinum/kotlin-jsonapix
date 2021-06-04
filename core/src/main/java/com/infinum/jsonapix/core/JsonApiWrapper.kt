package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.IncludedModel
import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiWrapper<out Model> {
    val data: ResourceObject<Model>?
    val included: IncludedModel?
    val errors: List<String>?
}
