package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiX<out Model> {
    val data: ResourceObject<Model>?
    val included: List<ResourceObject<*>>?
    val errors: List<Error>?

    val original: Model
}
