package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.ClassName

public data class JsonApiXErrorHolder(
    val type: String,
    val className: ClassName,
) : Holder
