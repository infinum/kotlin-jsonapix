package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.core.resources.ManyRelationshipMemberModel
import com.infinum.jsonapix.core.resources.OneRelationshipMemberModel
import com.infinum.jsonapix.core.resources.RelationshipsModel
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object RelationshipModelSpecBuilder {

    private const val GENERATED_NAME_PREFIX = "RelationshipsModel_"
    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        className: ClassName,
        type: String,
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): TypeSpec {
        val generatedName = "${GENERATED_NAME_PREFIX}${className.simpleName}"

        val properties: MutableList<PropertySpec> = oneRelationships.map {
            PropertySpec.builder(it.name, OneRelationshipMemberModel::class).initializer(it.name)
                .build()
        }.toMutableList()

        properties.addAll(
            manyRelationships.map {
                PropertySpec.builder(it.name, ManyRelationshipMemberModel::class)
                    .initializer(it.name).build()
            }
        )

        val params = properties.map {
            ParameterSpec.builder(it.name, it.type).addAnnotation(serialNameSpec(it.name)).build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(RelationshipsModel::class)
            .addAnnotation(serializableClassName)
            .addAnnotation(serialNameSpec(type))
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
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class)
            .addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

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
            builderArgs.add(OneRelationshipMemberModel::class.asClassName())
            builderArgs.add(ResourceIdentifier::class.asClassName())
            builderArgs.add(getTypeOfRelationship(property))
            if (index != oneRelationships.lastIndex
                || (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                constructorStringBuilder.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, property ->
            constructorStringBuilder.append("${property.name} = %T(originalObject.${property.name}.map { %T(%L) })")
            builderArgs.add(ManyRelationshipMemberModel::class.asClassName())
            builderArgs.add(ResourceIdentifier::class.asClassName())
            builderArgs.add(getTypeOfRelationship(property))
            if (index != manyRelationships.lastIndex) {
                constructorStringBuilder.append(", ")
            }
        }

        return FunSpec.builder("fromOriginalObject")
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build()
            )
            .addStatement("return %L(${constructorStringBuilder})", *builderArgs.toTypedArray())
            .build()
    }

    private fun getTypeOfRelationship(property: PropertySpec): String {
        return property.annotations.first { annotation ->
            annotation.typeName == HasOne::class.asTypeName() || annotation.typeName == HasMany::class.asTypeName()
        }.members.first { member ->
            member.toString().trim().startsWith("type")
        }.toString().split("=")[1].trim()
    }
}