package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.resources.IncludedModel
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal object JsonApiWrapperSpecBuilder {

    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val GENERATED_CLASS_PREFIX = "JsonApiSerializable_"
    private const val KEY_DATA = "data"
    private const val KEY_ERRORS = "errors"
    private const val INCLUDED_KEY = "included"
    private val serializableClassName = Serializable::class.asClassName()
    private val transientClassName = Transient::class.asClassName()

    fun build(
        pack: String,
        className: String,
        type: String
    ): FileSpec {
        val dataClass = ClassName(pack, className)
        val generatedName = "$GENERATED_CLASS_PREFIX$className"

        val properties = mutableListOf<PropertySpec>()
        val params = mutableListOf<ParameterSpec>()

        params.add(
            ParameterSpec.builder(
                KEY_DATA,
                ResourceObject::class.asClassName()
                    .parameterizedBy(dataClass)
            ).build()
        )

        properties.add(dataProperty(dataClass))

        params.add(
            nullParam(
                INCLUDED_KEY,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(Any::class.asClassName())
                ).copy(nullable = true)
            )
        )
        properties.add(
            nullProperty(
                INCLUDED_KEY,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(Any::class.asClassName())
                ).copy(nullable = true),
                true
            )
        )

        params.add(
            ParameterSpec.builder(
                KEY_ERRORS,
                List::class.parameterizedBy(String::class)
                    .copy(nullable = true)
            ).defaultValue("%L", "null")
                .build()
        )

        properties.add(errorsProperty())

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
                            .addParameters(params)
                            .build()
                    )
                    .addProperties(properties)
                    .build()
            )
            .build()
    }

    private fun namedProperty(pack: String, name: String, key: String): PropertySpec =
        PropertySpec.builder(
            key, ClassName(pack, name).copy(nullable = true), KModifier.OVERRIDE
        ).addAnnotation(
            serialNameSpec(key)
        )
            .initializer(key)
            .build()

    private fun namedParam(pack: String, name: String, key: String): ParameterSpec =
        ParameterSpec.builder(
            key,
            ClassName(pack, name).copy(nullable = true)
        ).defaultValue("%L", null)
            .build()

    private fun nullProperty(
        name: String,
        typeName: TypeName,
        isTransient: Boolean = false
    ): PropertySpec = PropertySpec.builder(
        name,
        typeName,
        KModifier.OVERRIDE
    ).apply {
        if (isTransient) {
            addAnnotation(transientClassName)
        } else {
            addAnnotation(serialNameSpec(name))
        }
        initializer(name)
    }.build()


    private fun nullParam(name: String, typeName: TypeName): ParameterSpec =
        ParameterSpec.builder(name, typeName, KModifier.OVERRIDE)
            .defaultValue("%L", null)
            .build()

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class).addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    private fun dataProperty(dataClass: ClassName): PropertySpec = PropertySpec.builder(
        KEY_DATA, ResourceObject::class.asClassName().parameterizedBy(dataClass)
    ).addAnnotation(
        serialNameSpec(KEY_DATA)
    )
        .initializer(KEY_DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        KEY_ERRORS,
        List::class.parameterizedBy(String::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(serialNameSpec(KEY_ERRORS))
        .initializer(KEY_ERRORS)
        .build()
}
