package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiX<out Model> {
    val data: ResourceObject<Model>?
    val included: List<ResourceObject<*>>?
    val errors: List<DefaultError>?
    val links: Links?
    val meta: Meta?

    val original: Model
}
