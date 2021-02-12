package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object ResourceObjectSpecBuilder {
    private const val ID_KEY = "id"
    private const val TYPE_KEY = "type"
    private const val GENERATED_CLASS_PREFIX = "ResourceObject_"
    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val ATTRIBUTES_KEY = "attributes"
    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        pack: String,
        className: String,
        type: String
    ): FileSpec {
        val dataClass = ClassName(pack, className)
        val generatedName = "${GENERATED_CLASS_PREFIX}$className"

        return FileSpec.builder(pack, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        ResourceObject::class.asClassName().parameterizedBy(dataClass)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(serialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                listOf(
                                    ParameterSpec.builder(
                                        ATTRIBUTES_KEY, dataClass
                                    ).build(),
                                    ParameterSpec.builder(
                                        ID_KEY, String::class
                                    ).defaultValue("%S", "0")
                                        .build(),
                                    ParameterSpec.builder(
                                        TYPE_KEY, String::class
                                    ).defaultValue("%S", type)
                                        .build()
                                )
                            )
                            .build()
                    )
                    .addProperties(
                        listOf(
                            idProperty(),
                            typeProperty(),
                            dataProperty(dataClass)
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class)
            .addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    private fun idProperty(): PropertySpec = PropertySpec.builder(
        ID_KEY, String::class, KModifier.OVERRIDE
    ).addAnnotation(serialNameSpec(ID_KEY))
        .initializer(ID_KEY)
        .build()

    private fun dataProperty(dataClass: ClassName): PropertySpec = PropertySpec.builder(
        ATTRIBUTES_KEY, dataClass
    ).addAnnotation(
        serialNameSpec(ATTRIBUTES_KEY)
    )
        .initializer(ATTRIBUTES_KEY).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun typeProperty(): PropertySpec = PropertySpec.builder(
        TYPE_KEY, String::class, KModifier.OVERRIDE
    ).addAnnotation(
        serialNameSpec(TYPE_KEY)
    ).initializer(TYPE_KEY).build()
}
