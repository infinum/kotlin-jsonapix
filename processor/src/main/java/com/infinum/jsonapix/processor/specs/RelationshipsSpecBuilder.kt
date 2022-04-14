package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal object RelationshipsSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        className: ClassName,
        type: String,
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): TypeSpec {
        val generatedName = JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName)

        val properties: MutableList<PropertySpec> = oneRelationships.map {
            PropertySpec.builder(it.name, OneRelationshipMember::class).initializer(it.name)
                .build()
        }.toMutableList()

        properties.addAll(
            manyRelationships.map {
                PropertySpec.builder(it.name, ManyRelationshipMember::class)
                    .initializer(it.name).build()
            }
        )

        val params = properties.map {
            ParameterSpec.builder(it.name, it.type).addAnnotation(Specs.getSerialNameSpec(it.name))
                .build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(Relationships::class)
            .addAnnotation(serializableClassName)
            .addAnnotation(
                Specs.getSerialNameSpec(
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(
                        type
                    )
                )
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(params)
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(
                        fromOriginalObjectSpec(
                            className,
                            generatedName,
                            oneRelationships,
                            manyRelationships
                        )
                    )
                    .build()
            )
            .addProperties(properties)
            .addProperty(linksPropertySpec(oneRelationships, manyRelationships))
            .build()
    }

    @SuppressWarnings("SpreadOperator")
    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): FunSpec {
        val constructorStringBuilder = StringBuilder()
        val builderArgs = mutableListOf<Any>(generatedName)
        oneRelationships.forEachIndexed { index, property ->
            constructorStringBuilder.append("${property.name} = %T(%T(%L))")
            builderArgs.add(OneRelationshipMember::class.asClassName())
            builderArgs.add(ResourceIdentifier::class.asClassName())
            builderArgs.add(getTypeOfRelationship(property))
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                constructorStringBuilder.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, property ->
            constructorStringBuilder.append(
                "${property.name} = %T(originalObject.${property.name}!!.map { %T(%L) })"
            )
            builderArgs.add(ManyRelationshipMember::class.asClassName())
            builderArgs.add(ResourceIdentifier::class.asClassName())
            builderArgs.add(getTypeOfRelationship(property))
            if (index != manyRelationships.lastIndex) {
                constructorStringBuilder.append(", ")
            }
        }

        return FunSpec.builder(JsonApiConstants.Members.FROM_ORIGINAL_OBJECT)
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build()
            )
            .addStatement("return %L($constructorStringBuilder)", *builderArgs.toTypedArray())
            .build()
    }

    private fun linksPropertySpec(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): PropertySpec {
        var returnStatement = "mapOf("
        oneRelationships.forEach {
            returnStatement += "\"${it.name}\" to ${it.name}.links, "
        }
        manyRelationships.forEach {
            returnStatement += "\"${it.name}\" to ${it.name}.links, "
        }
        returnStatement += ")"

        val builder = PropertySpec.builder(
            JsonApiConstants.Keys.LINKS,
            Map::class
                .asClassName()
                .parameterizedBy(
                    String::class.asTypeName(),
                    Links::class.asTypeName().copy(nullable = true)
                ),
            KModifier.OVERRIDE
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(returnStatement).build()
    }

    private fun getTypeOfRelationship(property: PropertySpec): String {
        return property.annotations.first { annotation ->
            annotation.typeName == HasOne::class.asTypeName() ||
                annotation.typeName == HasMany::class.asTypeName()
        }.members.first { member ->
            member.toString().trim().startsWith("type")
        }.toString().split("=")[1].trim()
    }
}
