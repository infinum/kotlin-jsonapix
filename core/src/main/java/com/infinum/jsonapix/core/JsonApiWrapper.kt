package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiWrapper<out T> {
    val data: ResourceObject<T>
    val errors: List<String>?
}