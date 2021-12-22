package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceObject
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
import kotlinx.serialization.Serializable

internal object ResourceObjectSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    @SuppressWarnings("LongMethod")
    fun build(
        className: ClassName,
        type: String,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>,
        links: ClassName?
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        val attributesClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
        )
        val relationshipsClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName)
        )

        val paramsList = mutableListOf<ParameterSpec>()
        val propsList = mutableListOf<PropertySpec>()

        paramsList.add(typeParam(type))
        paramsList.add(idParam())
        propsList.add(typeProperty())
        propsList.add(idProperty())

        if (attributes.isNotEmpty()) {
            paramsList.add(
                Specs.getNamedParamSpec(
                    attributesClassName,
                    JsonApiConstants.Keys.ATTRIBUTES,
                    nullable = true
                )
            )
            propsList.add(
                Specs.getNamedPropertySpec(
                    attributesClassName,
                    JsonApiConstants.Keys.ATTRIBUTES,
                    nullable = true
                )
            )
        } else {
            paramsList.add(
                Specs.getNullParamSpec(
                    JsonApiConstants.Keys.ATTRIBUTES,
                    Attributes::class.asClassName()
                        .copy(nullable = true)
                )
            )
            propsList.add(
                Specs.getNullPropertySpec(
                    JsonApiConstants.Keys.ATTRIBUTES,
                    Attributes::class.asClassName()
                        .copy(nullable = true),
                    isTransient = true
                )
            )
        }

        if (oneRelationships.isNotEmpty() || manyRelationships.isNotEmpty()) {
            paramsList.add(
                Specs.getNamedParamSpec(
                    relationshipsClassName,
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    nullable = true
                )
            )
            propsList.add(
                Specs.getNamedPropertySpec(
                    relationshipsClassName,
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    nullable = true
                )
            )
        } else {
            paramsList.add(
                Specs.getNullParamSpec(
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    Relationships::class.asClassName().copy(nullable = true)
                )
            )
            propsList.add(
                Specs.getNullPropertySpec(
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    Relationships::class.asClassName().copy(nullable = true),
                    isTransient = true
                )
            )
        }

        paramsList.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.LINKS,
                DefaultLinks::class.asClassName().copy(nullable = true)
            )
        )
        propsList.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.LINKS,
                DefaultLinks::class.asClassName().copy(nullable = true)
            )
        )

        return FileSpec.builder(className.packageName, generatedName)
            .addImport(JsonApiConstants.Packages.CORE_RESOURCES, JsonApiConstants.Imports.RESOURCE_IDENTIFIER)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        ResourceObject::class.asClassName().parameterizedBy(className)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(
                        Specs.getSerialNameSpec(
                            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(
                                type
                            )
                        )
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(paramsList)
                            .build()
                    )
                    .addFunction(
                        originalFunSpec(
                            className,
                            attributes,
                            oneRelationships,
                            manyRelationships
                        )
                    )
                    .addProperties(propsList)
                    .build()
            )
            .build()
    }

    @SuppressWarnings("SpreadOperator")
    private fun originalFunSpec(
        className: ClassName,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>
    ): FunSpec {
        var codeString = "return ${className.simpleName}("
        val builder = FunSpec.builder(JsonApiConstants.Members.ORIGINAL)
        builder.addModifiers(KModifier.OVERRIDE)
        builder.returns(className)
        builder.addParameter(
            JsonApiConstants.Keys.INCLUDED,
            List::class.asClassName().parameterizedBy(
                ResourceObject::class.asClassName().parameterizedBy(Any::class.asClassName())
            )
        )
        attributes.forEach {
            codeString += "${it.name} = attributes?.${it.name}"
            if (!it.type.isNullable) {
                codeString += "!!"
            }
            codeString += ", "
        }
        val typeParams = mutableListOf<TypeName>()
        oneRelationships.forEach {
            codeString +=
                """
                    ${it.key} = included.first { 
                    it.type == relationships!!.${it.key}.data.type && 
                    it.id == relationships.${it.key}.data.id }
                    .${JsonApiConstants.Members.ORIGINAL}(included) as %T, 
                """.trimIndent()
            typeParams.add(it.value)
        }
        manyRelationships.forEach {
            codeString +=
                """
                    ${it.key} = included.filter { relationships!!.${it.key}
                    .data.contains(ResourceIdentifier(it.type, it.id)) }
                    .map { it.${JsonApiConstants.Members.ORIGINAL}(included) } as %T, 
                """.trimIndent()
            typeParams.add(it.value)
        }
        codeString += ")"
        return builder.addStatement(codeString, *typeParams.toTypedArray()).build()
    }

    private fun idProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.ID, String::class, KModifier.OVERRIDE
    ).addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.ID))
        .initializer(JsonApiConstants.Keys.ID)
        .build()

    private fun idParam(): ParameterSpec = ParameterSpec.builder(
        JsonApiConstants.Keys.ID, String::class
    ).defaultValue("%S", "0")
        .build()

    private fun typeProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.TYPE, String::class, KModifier.OVERRIDE
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.TYPE)
    ).initializer(JsonApiConstants.Keys.TYPE).build()

    private fun typeParam(type: String): ParameterSpec = ParameterSpec.builder(
        JsonApiConstants.Keys.TYPE, String::class
    ).defaultValue("%S", type)
        .build()
}
