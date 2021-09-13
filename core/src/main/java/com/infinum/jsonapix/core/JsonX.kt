package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonX<out Model> {
    val data: ResourceObject<Model>?
    val included: List<ResourceObject<*>>?
    val errors: List<String>?

    fun toOriginal(): Model
}
