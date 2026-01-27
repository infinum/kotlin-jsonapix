package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.PropertySpec

internal data class HasManyHolder(
    val propertySpec: PropertySpec
) : Holder
