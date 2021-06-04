package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiWrapper<out Model> {
    val data: ResourceObject<Model>?
    val included: List<ResourceObject<Any>>?
    val errors: List<String>?
}
