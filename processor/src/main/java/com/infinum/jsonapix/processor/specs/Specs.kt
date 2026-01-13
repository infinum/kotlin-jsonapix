package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

internal object Specs {
    private val transientClassName = Transient::class.asClassName()

    fun getSerialNameSpec(name: String): AnnotationSpec =
        AnnotationSpec
            .builder(SerialName::class)
            .addMember(JsonApiConstants.SERIAL_NAME_FORMAT, name)
            .build()

    fun getNullPropertySpec(
        name: String,
        typeName: TypeName,
        isTransient: Boolean = false,
    ): PropertySpec =
        PropertySpec
            .builder(
                name,
                typeName,
                KModifier.OVERRIDE,
            ).apply {
                if (isTransient) {
                    addAnnotation(transientClassName)
                } else {
                    addAnnotation(getSerialNameSpec(name))
                }
                initializer(name)
            }.build()

    fun getNullParamSpec(
        name: String,
        typeName: TypeName,
    ): ParameterSpec =
        ParameterSpec
            .builder(name, typeName)
            .defaultValue("%L", null)
            .build()

    fun getNamedPropertySpec(
        className: ClassName,
        key: String,
        nullable: Boolean = false,
    ): PropertySpec =
        PropertySpec
            .builder(
                key,
                className.copy(nullable = nullable),
                KModifier.OVERRIDE,
            ).addAnnotation(
                getSerialNameSpec(key),
            ).initializer(key)
            .build()

    fun getNamedParamSpec(
        className: ClassName,
        key: String,
        nullable: Boolean = false,
    ): ParameterSpec =
        ParameterSpec
            .builder(
                key,
                className.copy(nullable = nullable),
            ).apply {
                if (nullable) {
                    defaultValue("%L", null)
                }
            }.build()
}
