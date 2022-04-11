package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta

@Suppress("UNCHECKED_CAST", "TooManyFunctions", "UnnecessaryAbstractClass")
abstract class JsonApiModel {
    private var rootLinks: Links? = null
    private var resourceLinks: Links? = null
    private var relationshipLinks: Map<String, Links?>? = null
    private var meta: Meta? = null

    fun setRootLinks(links: Links?) {
        rootLinks = links
    }

    fun setResourceLinks(links: Links?) {
        resourceLinks = links
    }

    fun setRelationshipsLinks(linkMap: Map<String, Links?>) {
        relationshipLinks = linkMap
    }

    fun setMeta(meta: Meta?) {
        this.meta = meta
    }

    fun rootLinks(): DefaultLinks? = rootLinks as? DefaultLinks

    fun <T : Links> rootLinks(): T? = rootLinks as? T

    fun resourceLinks(): DefaultLinks? = resourceLinks as? DefaultLinks

    fun <T : Links> resourceLinks(): T? = resourceLinks as? T

    fun relationshipsLinks(): Map<String, DefaultLinks?>? = relationshipLinks as? Map<String, DefaultLinks?>

    @JvmName("customRelationshipLinks")
    fun <T : Links> relationshipsLinks(): Map<String, T?>? = relationshipLinks as? Map<String, T?>

    fun <T : Meta> meta() = meta as? T
}
