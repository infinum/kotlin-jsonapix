package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiXList<out Model> {
    val data: List<ResourceObject<Model>>?
    val included: List<ResourceObject<*>>?
    val errors: List<String>?

    val original: List<Model>
}
