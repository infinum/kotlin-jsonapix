package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec

internal data class JsonApiXHolder(
    val className: ClassName,
    val type: String,
    val isNullable: Boolean,
    val primitiveProperties: List<PropertySpec>,
    val oneRelationships: List<PropertySpec>,
    val manyRelationships: List<PropertySpec>,
) : Holder
