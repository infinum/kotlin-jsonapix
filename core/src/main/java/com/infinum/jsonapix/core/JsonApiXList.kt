package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiXList<out Data, out Model> {
    val data: List<ResourceObject<Data>>?
    val included: List<ResourceObject<*>>?
    val errors: List<Error>?
    val links: Links?
    val meta: Meta?

    val original: Model
}
