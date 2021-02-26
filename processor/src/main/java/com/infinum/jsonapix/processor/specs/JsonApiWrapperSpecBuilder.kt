package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
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

internal object JsonApiWrapperSpecBuilder {

    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val GENERATED_CLASS_PREFIX = "JsonApiSerializable_"
    private const val DATA_KEY = "data"
    private const val ERRORS_TYPE_KEY = "errors"
    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        pack: String,
        className: String,
        type: String
    ): FileSpec {
        val dataClass = ClassName(pack, className)
        val generatedName = "$GENERATED_CLASS_PREFIX$className"

        return FileSpec.builder(pack, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        JsonApiWrapper::class.asClassName().parameterizedBy(dataClass)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(serialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                listOf(
                                    ParameterSpec.builder(
                                        DATA_KEY,
                                        ResourceObject::class.asClassName()
                                            .parameterizedBy(dataClass)
                                    ).build(),
                                    ParameterSpec.builder(
                                        ERRORS_TYPE_KEY,
                                        List::class.parameterizedBy(String::class)
                                            .copy(nullable = true)
                                    ).defaultValue("%L", "null")
                                        .build()
                                )
                            )
                            .build()
                    )
                    .addProperties(
                        listOf(
                            dataProperty(dataClass),
                            errorsProperty()
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class).addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    private fun dataProperty(dataClass: ClassName): PropertySpec = PropertySpec.builder(
        DATA_KEY, ResourceObject::class.asClassName().parameterizedBy(dataClass)
    ).addAnnotation(
        serialNameSpec(DATA_KEY)
    )
        .initializer(DATA_KEY).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        ERRORS_TYPE_KEY,
        List::class.parameterizedBy(String::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(serialNameSpec(ERRORS_TYPE_KEY))
        .initializer(ERRORS_TYPE_KEY)
        .build()
}
