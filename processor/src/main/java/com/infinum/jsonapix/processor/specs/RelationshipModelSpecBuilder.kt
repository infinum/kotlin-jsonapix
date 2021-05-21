package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.resources.ManyRelationshipMemberModel
import com.infinum.jsonapix.core.resources.OneRelationshipMemberModel
import com.infinum.jsonapix.core.resources.RelationshipsModel
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
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
            .addProperties(properties)
            .build()
    }

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class)
            .addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()
}