package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.processor.models.RelationshipProperty
import com.infinum.jsonapix.processor.specs.Specs
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

    @Suppress("LongMethod", "LongParameterList")
    fun build(
        className: ClassName,
        type: String,
        oneRelationships: List<RelationshipProperty>,
        manyRelationships: List<RelationshipProperty>,
    ): TypeSpec {
        val generatedName = JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName)
        val oneRelationshipProperties = oneRelationships.map { it.property }
        val manyRelationshipProperties = manyRelationships.map { it.property }

        val properties: MutableList<PropertySpec> = oneRelationships.map { relationship ->
            val builder = if (relationship.property.type.isNullable) {
                PropertySpec.builder(relationship.property.name, OneRelationshipMember::class.asTypeName().copy(nullable = true))
            } else {
                PropertySpec.builder(relationship.property.name, OneRelationshipMember::class)
            }

            builder
                .addSerialNameAnnotation(relationship)
                .initializer(relationship.property.name)
                .build()
        }.toMutableList()

        properties.addAll(
            manyRelationships.map { relationship ->
                val builder = if (relationship.property.type.isNullable) {
                    PropertySpec.builder(relationship.property.name, ManyRelationshipMember::class.asTypeName().copy(nullable = true))
                } else {
                    PropertySpec.builder(relationship.property.name, ManyRelationshipMember::class)
                }

                builder
                    .addSerialNameAnnotation(relationship)
                    .initializer(relationship.property.name)
                    .build()
            },
        )

        val params = mapPropertiesToParams(properties)

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(Relationships::class)
            .addAnnotation(serializableClassName)
            .addAnnotation(
                Specs.getSerialNameSpec(
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(
                        type,
                    ),
                ),
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(params)
                    .build(),
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(
                        fromOriginalObjectSpec(
                            className,
                            generatedName,
                            oneRelationships,
                            manyRelationships,
                        ),
                    )
                    .build(),
            )
            .addProperties(properties)
            .addProperty(linksPropertySpec(oneRelationshipProperties, manyRelationshipProperties))
            .addProperty(metaPropertySpec(oneRelationshipProperties, manyRelationshipProperties))
            .build()
    }

    private fun PropertySpec.Builder.addSerialNameAnnotation(relationship: RelationshipProperty): PropertySpec.Builder =
        addAnnotation(Specs.getSerialNameSpec(relationship.serialName ?: relationship.property.name))

    private fun mapPropertiesToParams(properties: List<PropertySpec>): List<ParameterSpec> {
        return properties.map {
            ParameterSpec.builder(it.name, it.type)
                .apply {
                    if (it.type.isNullable) {
                        defaultValue("%L", null)
                    }
                }
                .build()
        }
    }

    @SuppressWarnings("SpreadOperator", "StringLiteralDuplication")
    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        oneRelationships: List<RelationshipProperty>,
        manyRelationships: List<RelationshipProperty>,
    ): FunSpec {
        val constructorStringBuilder = StringBuilder()
        val builderArgs = mutableListOf<Any>(generatedName)
        oneRelationships.forEachIndexed { index, relationship ->
            val property = relationship.property
            if (property.type.isNullable) {
                constructorStringBuilder.append(
                    "${property.name} = originalObject.${property.name}" +
                        "?.toOneRelationshipModel(%S, (originalObject.${property.name} as? JsonApiModel)?.id().orEmpty())",
                )
            } else {
                constructorStringBuilder.append(
                    "${property.name} = originalObject.${property.name}." +
                        "toOneRelationshipModel(%S, (originalObject.${property.name} as? JsonApiModel)?.id().orEmpty())",
                )
            }

            builderArgs.add(relationship.type)
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                constructorStringBuilder.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, relationship ->
            val property = relationship.property
            if (property.type.isNullable) {
                constructorStringBuilder.append(
                    "${property.name} = originalObject.${property.name}" +
                        "?.toManyRelationshipModel(%S, { (it as? JsonApiModel)?.id().orEmpty() })",
                )
            } else {
                constructorStringBuilder.append(
                    "${property.name} = originalObject.${property.name}" +
                        ".toManyRelationshipModel(%S, { (it as? JsonApiModel)?.id().orEmpty() })",
                )
            }
            builderArgs.add(relationship.type)
            if (index != manyRelationships.lastIndex) {
                constructorStringBuilder.append(", ")
            }
        }

        return FunSpec.builder(JsonApiConstants.Members.FROM_ORIGINAL_OBJECT)
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build(),
            )
            .addStatement("return %L($constructorStringBuilder)", *builderArgs.toTypedArray())
            .returns(ClassName.bestGuess(generatedName))
            .build()
    }

    private fun linksPropertySpec(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>,
    ): PropertySpec {
        var returnStatement = "mapOf("
        oneRelationships.forEach {
            returnStatement += if (it.type.isNullable) {
                "\"${it.name}\" to ${it.name}?.links, "
            } else {
                "\"${it.name}\" to ${it.name}.links, "
            }
        }
        manyRelationships.forEach {
            returnStatement += if (it.type.isNullable) {
                "\"${it.name}\" to ${it.name}?.links, "
            } else {
                "\"${it.name}\" to ${it.name}.links, "
            }
        }
        returnStatement += ")"

        val builder = PropertySpec.builder(
            JsonApiConstants.Keys.LINKS,
            Map::class
                .asClassName()
                .parameterizedBy(
                    String::class.asTypeName(),
                    Links::class.asTypeName().copy(nullable = true),
                ),
            KModifier.OVERRIDE,
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(returnStatement).build()
    }

    private fun metaPropertySpec(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>,
    ): PropertySpec {
        var returnStatement = "mapOf("
        oneRelationships.forEach {
            returnStatement += if (it.type.isNullable) {
                "\"${it.name}\" to ${it.name}?.meta, "
            } else {
                "\"${it.name}\" to ${it.name}.meta, "
            }
        }
        manyRelationships.forEach {
            returnStatement += if (it.type.isNullable) {
                "\"${it.name}\" to ${it.name}?.meta, "
            } else {
                "\"${it.name}\" to ${it.name}.meta, "
            }
        }
        returnStatement += ")"

        val builder = PropertySpec.builder(
            JsonApiConstants.Keys.META,
            Map::class
                .asClassName()
                .parameterizedBy(
                    String::class.asTypeName(),
                    Meta::class.asTypeName().copy(nullable = true),
                ),
            KModifier.OVERRIDE,
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(returnStatement).build()
    }
}
