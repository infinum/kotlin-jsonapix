package com.infinum.jsonapix.processor

public data class LinksInfo(
    val type: String,
    var rootLinks: String? = null,
    var resourceObjectLinks: String? = null,
    var relationshipsLinks: String? = null
)