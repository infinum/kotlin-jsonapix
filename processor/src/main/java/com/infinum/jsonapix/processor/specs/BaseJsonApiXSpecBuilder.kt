package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.processor.MetaInfo
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Contextual

internal abstract class BaseJsonApiXSpecBuilder {

    abstract fun build(
        className: ClassName,
        isNullable: Boolean,
        type: String,
        metaInfo: MetaInfo?,
    ): FileSpec

    fun getBaseParamSpecs(metaClassName: ClassName?): List<ParameterSpec> {
        val params = mutableListOf<ParameterSpec>()

        params.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.ERRORS,
                List::class.parameterizedBy(Error::class)
                    .copy(nullable = true)
            ).defaultValue("%L", "null")
                .build()
        )

        params.add(
            Specs.getNamedParamSpec(
                Links::class.asClassName(),
                JsonApiConstants.Keys.LINKS,
                true
            )
        )

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.META,
                metaClassName?.copy(nullable = true) ?: Meta::class.asClassName()
                    .copy(nullable = true)
            ).defaultValue("%L", "null").build()
        )
        return params
    }

    fun getBasePropertySpecs(metaClassName: ClassName?): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        properties.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )
        properties.add(errorsProperty())

        properties.add(
            Specs.getNamedPropertySpec(
                Links::class.asClassName(),
                JsonApiConstants.Keys.LINKS,
                true
            )
        )
        properties.add(metaProperty(metaClassName))

        return properties
    }

    private fun getAnnotatedAnyType(): TypeName {
        val contextual = AnnotationSpec.builder(Contextual::class).build()
        return ANY.copy(annotations = ANY.annotations + contextual)
    }

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.ERRORS,
        List::class.parameterizedBy(Error::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.ERRORS))
        .initializer(JsonApiConstants.Keys.ERRORS)
        .build()

    private fun metaProperty(
        metaClassName: ClassName?
    ): PropertySpec {
        return PropertySpec.builder(
            JsonApiConstants.Keys.META,
            metaClassName?.copy(nullable = true) ?: Meta::class.asClassName().copy(nullable = true),
            KModifier.OVERRIDE
        ).addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.META))
            .initializer(JsonApiConstants.Keys.META)
            .build()
    }
}
