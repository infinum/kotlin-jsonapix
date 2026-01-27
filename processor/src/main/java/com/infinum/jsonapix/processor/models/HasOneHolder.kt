package com.infinum.jsonapix.processor.models

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

internal data class HasOneHolder(
    val propertySpec: PropertySpec
) : Holder
