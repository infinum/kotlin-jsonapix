package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta

@Suppress("UNCHECKED_CAST", "TooManyFunctions", "UnnecessaryAbstractClass")
abstract class JsonApiModel {
    private var type: String? = null
    private var id: String? = "0"
    private var rootLinks: Links? = null
    private var resourceLinks: Links? = null
    private var relationshipLinks: Map<String, Links?>? = null
    private var errors: List<Error>? = null
    private var meta: Meta? = null

    fun setType(type: String?) {
        this.type = type
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun setRootLinks(links: Links?) {
        rootLinks = links
    }

    fun setErrors(errors: List<Error>?) {
        this.errors = errors
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

    fun type(): String? = type

    fun id(): String? = id

    fun rootLinks(): DefaultLinks? = rootLinks as? DefaultLinks

    fun errors(): List<Error>? = errors as? List<DefaultError>

    fun <T : Links> rootLinks(): T? = rootLinks as? T

    fun resourceLinks(): DefaultLinks? = resourceLinks as? DefaultLinks

    fun <T : Links> resourceLinks(): T? = resourceLinks as? T

    fun relationshipsLinks(): Map<String, DefaultLinks?>? = relationshipLinks as? Map<String, DefaultLinks?>

    @JvmName("customRelationshipLinks")
    fun <T : Links> relationshipsLinks(): Map<String, T?>? = relationshipLinks as? Map<String, T?>

    fun <T : Meta> meta() = meta as? T
}
