package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.ClassName

internal data class JsonApiXErrorResult(
    val customErrors: Map<String, ClassName>
) {
    companion object {
        val EMPTY = JsonApiXErrorResult(emptyMap())
    }
}
