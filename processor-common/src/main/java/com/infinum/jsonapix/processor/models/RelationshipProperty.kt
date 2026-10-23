package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.PropertySpec

/**
 * A model property annotated with `@HasOne` or `@HasMany`, whose JSON:API type is [type].
 *
 * [property] doesn't carry the model's `@SerialName` annotation. Its value is [serialName] instead.
 */
public data class RelationshipProperty(
    val property: PropertySpec,
    val type: String,
    val serialName: String?,
)
