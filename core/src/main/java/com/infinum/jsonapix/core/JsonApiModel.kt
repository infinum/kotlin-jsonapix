package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Links

abstract class JsonApiModel {
    private var rootLinks: Links? = null

    fun setRootLinks(links: Links?) {
        rootLinks = links
    }

    fun rootLinks(): DefaultLinks? = rootLinks as? DefaultLinks

    fun <T: Links> rootLinks(): T? = rootLinks as? T
}