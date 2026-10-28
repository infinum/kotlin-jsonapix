package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.processor.models.AttributeProperty
import com.infinum.jsonapix.processor.specs.Specs
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Serializable

internal object AttributesSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        className: ClassName,
        attributes: List<AttributeProperty>,
        type: String,
    ): TypeSpec {
        val generatedName = JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
        val properties = attributes.map { it.property }

        val parameterSpecs = attributes.map { attribute ->
            val property = attribute.property
            ParameterSpec.builder(property.name, property.type)
                .addAnnotation(Specs.getSerialNameSpec(attribute.serialName ?: property.name))
                .apply {
                    if (property.type.isNullable) {
                        defaultValue("%L", null)
                    }
                }
                .build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(Attributes::class.asClassName())
            .addAnnotation(serializableClassName)
            .addAnnotation(
                Specs.getSerialNameSpec(JsonApiConstants.Prefix.ATTRIBUTES.withName(type)),
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(parameterSpecs)
                    .build(),
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(fromOriginalObjectSpec(className, generatedName, properties))
                    .build(),
            )
            .addProperties(properties)
            .build()
    }

    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        attributes: List<PropertySpec>,
    ): FunSpec {
        val constructorString = attributes.joinToString(", ") {
            "${it.name} = originalObject.${it.name}"
        }
        return FunSpec.builder(JsonApiConstants.Members.FROM_ORIGINAL_OBJECT)
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build(),
            )
            .addStatement("return %L($constructorString)", generatedName)
            .returns(ClassName.bestGuess(generatedName))
            .build()
    }
}
