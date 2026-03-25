package com.infinum.jsonapix.processor.specs.models

import com.squareup.kotlinpoet.ClassName

@Suppress("DataClassShouldBeImmutable")
public data class MetaInfo(
    val type: String,
    var rootClassName: ClassName? = null,
    var resourceObjectClassName: ClassName? = null,
    var relationshipsClassNAme: ClassName? = null,
)
