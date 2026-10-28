package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.ClassName

public data class JsonApiXHolder(
    val className: ClassName,
    val type: String,
    val isNullable: Boolean,
    val attributes: List<AttributeProperty>,
    val oneRelationships: List<RelationshipProperty>,
    val manyRelationships: List<RelationshipProperty>,
) : Holder
