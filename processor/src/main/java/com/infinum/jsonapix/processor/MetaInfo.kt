package com.infinum.jsonapix.processor

public data class MetaInfo(
    val type: String,
    var rootMeta: String? = null,
    var resourceObjectMeta: String? = null,
    var relationshipsMeta: String? = null
)
