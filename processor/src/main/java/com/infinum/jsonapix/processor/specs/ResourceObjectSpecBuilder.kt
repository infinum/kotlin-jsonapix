package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Relationships
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

object ResourceObjectSpecBuilder {
    private const val ID_KEY = "id"
    private const val TYPE_KEY = "type"
    private const val GENERATED_CLASS_PREFIX = "ResourceObject_"
    private const val ATTRIBUTES_PREFIX = "AttributesModel_"
    private const val RELATIONSHIPS_PREFIX = "RelationshipsModel_"
    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val ATTRIBUTES_KEY = "attributes"
    private const val RELATIONSHIPS_KEY = "relationships"
    private const val LINKS_KEY = "links"
    private val serializableClassName = Serializable::class.asClassName()
    private val transientClassName = Transient::class.asClassName()

    fun build(
        pack: String,
        className: String,
        type: String,
        hasPrimitives: Boolean,
        hasComposites: Boolean
    ): FileSpec {
        val dataClass = ClassName(pack, className)
        val generatedName = "$GENERATED_CLASS_PREFIX$className"
        val attributesClassName = "$ATTRIBUTES_PREFIX$className"
        val relationshipsClassName = "$RELATIONSHIPS_PREFIX$className"

        val paramsList = mutableListOf<ParameterSpec>()
        val propsList = mutableListOf<PropertySpec>()

        paramsList.add(typeParam(type))
        paramsList.add(idParam())
        propsList.add(typeProperty())
        propsList.add(idProperty())

        if (hasPrimitives) {
            paramsList.add(namedParam(pack, attributesClassName, ATTRIBUTES_KEY))
            propsList.add(namedProperty(pack, attributesClassName, ATTRIBUTES_KEY))
        } else {
            paramsList.add(
                nullParam(
                    ATTRIBUTES_KEY,
                    Attributes::class.asClassName().parameterizedBy(ClassName(pack, className))
                        .copy(nullable = true)
                )
            )
            propsList.add(
                nullProperty(
                    ATTRIBUTES_KEY,
                    Attributes::class.asClassName().parameterizedBy(ClassName(pack, className)).copy(nullable = true),
                    true
                )
            )
        }

        if (hasComposites) {
            paramsList.add(namedParam(pack, relationshipsClassName, RELATIONSHIPS_KEY))
            propsList.add(namedProperty(pack, relationshipsClassName, RELATIONSHIPS_KEY))
        } else {
            paramsList.add(
                nullParam(
                    RELATIONSHIPS_KEY,
                    Relationships::class.asClassName().copy(nullable = true)
                )
            )
            propsList.add(
                nullProperty(
                    RELATIONSHIPS_KEY,
                    Relationships::class.asClassName().copy(nullable = true),
                    true
                )
            )
        }

        paramsList.add(nullParam(LINKS_KEY, Links::class.asClassName().copy(nullable = true)))
        propsList.add(
            nullProperty(
                LINKS_KEY,
                Links::class.asClassName().copy(nullable = true)
            )
        )

        return FileSpec.builder(pack, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        ResourceObject::class.asClassName().parameterizedBy(dataClass)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(serialNameSpec("$GENERATED_CLASS_PREFIX$type"))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(paramsList)
                            .build()
                    )
                    .addProperties(propsList)
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

    private fun idParam(): ParameterSpec = ParameterSpec.builder(
        ID_KEY, String::class
    ).defaultValue("%S", "0")
        .build()

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

    private fun typeProperty(): PropertySpec = PropertySpec.builder(
        TYPE_KEY, String::class, KModifier.OVERRIDE
    ).addAnnotation(
        serialNameSpec(TYPE_KEY)
    ).initializer(TYPE_KEY).build()

    private fun typeParam(type: String): ParameterSpec = ParameterSpec.builder(
        TYPE_KEY, String::class
    ).defaultValue("%S", type)
        .build()
}
