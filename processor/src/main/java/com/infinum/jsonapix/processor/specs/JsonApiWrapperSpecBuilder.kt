package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.processor.JsonApiWrapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.SerialName

internal object JsonApiWrapperSpecBuilder {

    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val GENERATED_CLASS_PREFIX = "JsonApiSerializable_"
    private const val DATA_KEY = "data"
    private const val ID_KEY = "id"
    private const val TYPE_KEY = "type"
    private val serializableClassName = ClassName("kotlinx.serialization", "Serializable")

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
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder(DATA_KEY, dataClass).build()
                            )
                            .build()
                    )
                    .addProperties(
                        listOf(
                            idProperty(),
                            typeProperty(type),
                            dataProperty(dataClass)
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class).addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    // TODO id doesn't need to have a value, but it needs to be present here because API should return id
    private fun idProperty(): PropertySpec = PropertySpec.builder(
        ID_KEY, Int::class, KModifier.OVERRIDE
    ).addAnnotation(serialNameSpec(ID_KEY))
        .initializer("%L", 0).build()

    private fun dataProperty(dataClass: ClassName): PropertySpec = PropertySpec.builder(
        DATA_KEY, dataClass
    ).addAnnotation(
        serialNameSpec(DATA_KEY)
    )
        .initializer(DATA_KEY).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun typeProperty(type: String): PropertySpec = PropertySpec.builder(
        TYPE_KEY, String::class, KModifier.OVERRIDE
    ).addAnnotation(
        serialNameSpec(TYPE_KEY)
    ).initializer("%S", type).build()
}