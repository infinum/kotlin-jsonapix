package com.infinum.jsonapix.processor.specs.models

import com.squareup.kotlinpoet.ClassName

@Suppress("DataClassShouldBeImmutable")
public data class LinksInfo(
    val type: String,
    var rootLinks: ClassName? = null,
    var resourceObjectLinks: ClassName? = null,
    var relationshipsLinks: ClassName? = null,
)
