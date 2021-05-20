package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object ResourceIdentifierSpecBuilder {

    private const val ID_KEY = "id"
    private const val TYPE_KEY = "type"
    private const val GENERATED_CLASS_PREFIX = "ResourceIdentifier_"
    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private val serializableClassName = Serializable::class.asClassName()

    fun build(packageName: String, className: String, type: String): FileSpec {
        val generatedName = "$GENERATED_CLASS_PREFIX$className"
        val generatedClassName = ClassName(packageName, generatedName)
        val parameters = listOf(
            ParameterSpec.builder(TYPE_KEY, String::class, KModifier.OVERRIDE)
                .addAnnotation(serialNameSpec(TYPE_KEY))
                .defaultValue("%S", type)
                .build(),
            ParameterSpec.builder(ID_KEY, String::class, KModifier.OVERRIDE)
                .addAnnotation(serialNameSpec(ID_KEY))
                .defaultValue("%S", "0")
                .build(),
        )

        return FileSpec.builder(packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedClassName)
                    .addSuperinterface(ResourceIdentifier::class.asClassName())
                    .addAnnotation(serializableClassName)
                    .addAnnotation(serialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(parameters)
                            .build()
                    )
                    .addProperties(parameters.map { PropertySpec.builder(it.name, it.type).initializer(it.name).build() })
                    .build()
            )
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class)
            .addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()
}