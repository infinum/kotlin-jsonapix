package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.AttributesModel
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AttributesModelSpecBuilder {

    private const val GENERATED_NAME_PREFIX = "AttributesModel_"
    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        attributes: List<PropertySpec>,
        className: ClassName,
        type: String
    ): TypeSpec {
        val generatedName = "$GENERATED_NAME_PREFIX${className.simpleName}"
        val parameterSpecs = attributes.map {
            ParameterSpec.builder(it.name, it.type)
                .addAnnotation(serialNameSpec(it.name))
                .build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(AttributesModel::class)
            .addAnnotation(serializableClassName)
            .addAnnotation(serialNameSpec(type))
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
            .addProperties(attributes)
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class)
            .addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        attributes: List<PropertySpec>
    ): FunSpec {
        val constructorString = attributes.joinToString(", ") {
            "${it.name} = originalObject.${it.name}"
        }
        return FunSpec.builder("fromOriginalObject")
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build()
            )
            .addStatement("return %L($constructorString)", generatedName)
            .build()
    }
}
