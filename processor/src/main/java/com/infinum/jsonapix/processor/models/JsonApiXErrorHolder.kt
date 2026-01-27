package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.ClassName

internal data class JsonApiXErrorHolder(
    val type: String,
    val className: ClassName
) : Holder
