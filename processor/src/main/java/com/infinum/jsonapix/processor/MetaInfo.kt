package com.infinum.jsonapix.processor

import com.squareup.kotlinpoet.ClassName

public data class MetaInfo(
    val type: String,
    var rootClassName: ClassName? = null,
    var resourceObjectClassName: ClassName? = null,
    var relationshipsClassNAme: ClassName? = null,
)
