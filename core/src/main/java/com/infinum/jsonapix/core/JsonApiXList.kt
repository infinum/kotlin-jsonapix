package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ResourceObject

interface JsonApiXList<out Model> {
    val data: List<ResourceObject<Model>>?
    val included: List<ResourceObject<*>>?
    val errors: List<Error>?
    val links: Links?

    val original: List<Model>

    fun resourceObjectsLinks(): List<DefaultLinks> {
        return data?.mapNotNull { it.links as? DefaultLinks }.orEmpty()
    }
}
