package com.infinum.jsonapix.processor.models

import com.infinum.jsonapix.processor.LinksInfo
import com.squareup.kotlinpoet.ClassName

internal data class JsonApiXLinksResult(
    val linksInfoMap: Map<String, LinksInfo>,
    val customLinksClassNames: List<ClassName>
) {
    companion object {
        val EMPTY = JsonApiXLinksResult(emptyMap(), emptyList())
    }
}
