package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.PropertySpec

/**
 * A model property generated as a JSON:API attribute.
 *
 * [property] doesn't carry the model's `@SerialName` annotation. Its value is [serialName] instead.
 */
public data class AttributeProperty(
    val property: PropertySpec,
    val serialName: String?,
)
