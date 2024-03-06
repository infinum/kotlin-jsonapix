package com.infinum.jsonapix.processor

import com.squareup.kotlinpoet.ClassName

public data class LinksInfo(
    val type: String,
    var rootLinks: ClassName? = null,
    var resourceObjectLinks: ClassName? = null,
    var relationshipsLinks: ClassName? = null
)
