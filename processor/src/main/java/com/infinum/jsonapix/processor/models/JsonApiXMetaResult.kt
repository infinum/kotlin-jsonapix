package com.infinum.jsonapix.processor.models

import com.infinum.jsonapix.processor.MetaInfo
import com.squareup.kotlinpoet.ClassName

internal data class JsonApiXMetaResult(
    val metaInfoMap: Map<String, MetaInfo>,
    val customMetaClassNames: List<ClassName>
) {
    companion object {
        val EMPTY = JsonApiXMetaResult(emptyMap(), emptyList())
    }
}
