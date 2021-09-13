package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.Attributes
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Serializable

internal object AttributesSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        className: ClassName,
        attributes: List<PropertySpec>,
        type: String,
        hasRelationships: Boolean
    ): TypeSpec {
        val generatedName = JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
        val parameterSpecs = attributes.map {
            ParameterSpec.builder(it.name, it.type)
                .addAnnotation(Specs.getSerialNameSpec(it.name))
                .build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(Attributes::class.asClassName().parameterizedBy(className))
            .addAnnotation(serializableClassName)
            .addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Prefix.ATTRIBUTES.withName(type)))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(parameterSpecs)
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(fromOriginalObjectSpec(className, generatedName, attributes))
                    .build()
            )
            .addFunction(toOriginalOrNullFunSpec(attributes, hasRelationships, className))
            .addProperties(attributes)
            .build()
    }

    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        attributes: List<PropertySpec>
    ): FunSpec {
        val constructorString = attributes.joinToString(", ") {
            "${it.name} = originalObject.${it.name}"
        }
        return FunSpec.builder(JsonApiConstants.Members.FROM_ORIGINAL_OBJECT)
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build()
            )
            .addStatement("return %L($constructorString)", generatedName)
            .build()
    }

    private fun toOriginalOrNullFunSpec(
        attributes: List<PropertySpec>,
        hasRelationships: Boolean,
        returnType: ClassName
    ): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.TO_ORIGINAL_OR_NULL)
            .addModifiers(KModifier.OVERRIDE)
            .returns(returnType.copy(nullable = true))
            .apply {
                if (hasRelationships) {
                    addStatement(JsonApiConstants.Statements.RETURN_NULL)
                } else {
                    val constructorString = attributes.joinToString(", ") {
                        "${it.name} = ${it.name}"
                    }
                    addStatement("return %T($constructorString)", returnType)
                }
            }
            .build()
    }
}
