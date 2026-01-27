package com.infinum.jsonapix.processor.models

import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec

internal data class JsonApiXHolder(
    val className: ClassName,
    val type: String,
    val isNullable: Boolean,
    val primitiveProperties: List<PropertySpec>,
    val oneRelationships: List<HasOneHolder>,
    val manyRelationships: List<HasManyHolder>,
    val metaInfo: MetaInfo?,
    val linksInfo: LinksInfo?,
    val customError: ClassName?
) : Holder
