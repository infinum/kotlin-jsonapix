package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.AttributesModel
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AttributesModelSpecBuilder {

    private const val GENERATED_NAME_PREFIX = "AttributesModel_"
    private val serializableClassName = Serializable::class.asClassName()


    fun build(
        attributes: List<PropertySpec>,
        className: String
    ): TypeSpec {
        val generatedName = "$GENERATED_NAME_PREFIX$className"
        val parameterSpecs = attributes.map {
            ParameterSpec.builder(it.name, it.type).build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(AttributesModel::class)
            .addAnnotation(serializableClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(parameterSpecs)
                    .build()
            )
            .addProperties(attributes)
            .build()
    }
}