package com.infinum.jsonapix.core.extensions

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.resources.Links

inline fun <reified LinksModel : Links> JsonApiX<*>.customResourceObjectLinks(): LinksModel? {
    return data?.links as? LinksModel
}

inline fun <reified LinksModel : Links> JsonApiXList<*>.customResourceObjectsLinks(): List<LinksModel> {
    return data?.mapNotNull { it.links as? LinksModel }.orEmpty()
}