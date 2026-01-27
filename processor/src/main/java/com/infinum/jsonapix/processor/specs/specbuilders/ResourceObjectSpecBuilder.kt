package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.Specs
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
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

    @SuppressWarnings("LongMethod", "LongParameterList")
    fun build(
        className: ClassName,
        metaClassName: ClassName?,
        linksInfo: LinksInfo?,
        type: String,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>,
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        val attributesClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName),
        )
        val relationshipsClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName),
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
                    nullable = true,
                ),
            )
            propsList.add(
                Specs.getNamedPropertySpec(
                    attributesClassName,
                    JsonApiConstants.Keys.ATTRIBUTES,
                    nullable = true,
                ),
            )
        } else {
            paramsList.add(
                Specs.getNullParamSpec(
                    JsonApiConstants.Keys.ATTRIBUTES,
                    Attributes::class.asClassName()
                        .copy(nullable = true),
                ),
            )
            propsList.add(
                Specs.getNullPropertySpec(
                    JsonApiConstants.Keys.ATTRIBUTES,
                    Attributes::class.asClassName()
                        .copy(nullable = true),
                    isTransient = true,
                ),
            )
        }

        if (oneRelationships.isNotEmpty() || manyRelationships.isNotEmpty()) {
            paramsList.add(
                Specs.getNamedParamSpec(
                    relationshipsClassName,
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    nullable = true,
                ),
            )
            propsList.add(
                Specs.getNamedPropertySpec(
                    relationshipsClassName,
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    nullable = true,
                ),
            )
        } else {
            paramsList.add(
                Specs.getNullParamSpec(
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    Relationships::class.asClassName().copy(nullable = true),
                ),
            )
            propsList.add(
                Specs.getNullPropertySpec(
                    JsonApiConstants.Keys.RELATIONSHIPS,
                    Relationships::class.asClassName().copy(nullable = true),
                    isTransient = true,
                ),
            )
        }

        paramsList.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.LINKS,
                linksInfo?.resourceObjectLinks?.copy(nullable = true) ?: DefaultLinks::class.asClassName().copy(nullable = true),
            ),
        )
        propsList.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.LINKS,
                linksInfo?.resourceObjectLinks?.copy(nullable = true) ?: DefaultLinks::class.asClassName().copy(nullable = true),
            ),
        )

        paramsList.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.META,
                metaClassName?.copy(nullable = true) ?: Meta::class.asClassName().copy(nullable = true),
            ),
        )
        propsList.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.META,
                metaClassName?.copy(nullable = true) ?: Meta::class.asClassName().copy(nullable = true),
            ),
        )

        return FileSpec.builder(className.packageName, generatedName)
            .addImport(JsonApiConstants.Packages.CORE_RESOURCES, JsonApiConstants.Imports.RESOURCE_IDENTIFIER)
            .addImport(JsonApiConstants.Packages.CORE_SHARED, JsonApiConstants.Imports.REQUIRE_NOT_NULL)
            .addImport(JsonApiConstants.Packages.CORE, JsonApiConstants.Imports.JSON_API_MODEL)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        ResourceObject::class.asClassName().parameterizedBy(className),
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(
                        Specs.getSerialNameSpec(
                            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(
                                type,
                            ),
                        ),
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(paramsList)
                            .build(),
                    )
                    .addFunction(
                        originalFunSpec(
                            className,
                            attributes,
                            oneRelationships,
                            manyRelationships,
                        ),
                    )
                    .addProperties(propsList)
                    .build(),
            )
            .build()
    }

    @SuppressWarnings("SpreadOperator", "LongMethod", "CognitiveComplexMethod")
    private fun originalFunSpec(
        className: ClassName,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>,
    ): FunSpec {
        val tempVariableName = "tempOriginal"

        val builder = FunSpec.builder(JsonApiConstants.Members.ORIGINAL)
        builder.addModifiers(KModifier.OVERRIDE)
        builder.returns(className)
        builder.addParameter(
            JsonApiConstants.Keys.INCLUDED,
            List::class.asClassName().parameterizedBy(
                ResourceObject::class.asClassName().parameterizedBy(Any::class.asClassName()),
            ).copy(nullable = true),
        )

        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.addStatement("val $tempVariableName = %T(", className).indent()
        attributes.forEach {
            if (it.type.isNullable) {
                codeBlockBuilder.addStatement(
                    "%N = attributes?.%N,",
                    it.name,
                    it.name,
                )
            } else {
                codeBlockBuilder.addStatement(
                    "%N = requireNotNull(attributes?.%N, %S),",
                    it.name,
                    it.name,
                    it.name,
                )
            }
        }

        oneRelationships.forEach {
            codeBlockBuilder.addStatement("%N = relationships?.let { safeRelationships ->", it.key)
            codeBlockBuilder.indent().addStatement("included?.firstOrNull {")
            if (it.value.isNullable) {
                codeBlockBuilder.indent().addStatement(
                    "safeRelationships.%N?.data == ResourceIdentifier(it.type, it.id)",
                    it.key,
                )
            } else {
                codeBlockBuilder.indent().addStatement(
                    "safeRelationships.%N.data == ResourceIdentifier(it.type, it.id)",
                    it.key,
                )
            }
            codeBlockBuilder.unindent()
            codeBlockBuilder.addStatement("}?.${JsonApiConstants.Members.ORIGINAL}(included) as %T", it.value)
            if (it.value.isNullable) {
                codeBlockBuilder.unindent().addStatement("},")
            } else {
                codeBlockBuilder.unindent().addStatement(
                    "} ?: throw %T(%S),",
                    JsonApiXMissingArgumentException::class,
                    it.key,
                )
            }
        }

        manyRelationships.forEach {
            codeBlockBuilder.addStatement("%N = relationships?.let { safeRelationships ->", it.key)
            codeBlockBuilder.indent().addStatement("included?.filter {")
            if (it.value.isNullable) {
                codeBlockBuilder.indent().addStatement(
                    "safeRelationships.%N?.data?.contains(ResourceIdentifier(it.type, it.id)) == true",
                    it.key,
                )
            } else {
                codeBlockBuilder.indent().addStatement(
                    "safeRelationships.%N.data.contains(ResourceIdentifier(it.type, it.id))",
                    it.key,
                )
            }
            codeBlockBuilder.unindent().addStatement(
                "}?.map { it.${JsonApiConstants.Members.ORIGINAL}(included) }.takeIf { it?.isNotEmpty() == true } as %T",
                it.value,
            )
            if (it.value.isNullable) {
                codeBlockBuilder.unindent().addStatement("},")
            } else {
                codeBlockBuilder.unindent().addStatement(
                    "} ?: throw %T(%S),",
                    JsonApiXMissingArgumentException::class,
                    it.key,
                )
            }
        }
        codeBlockBuilder.addStatement(")")

        codeBlockBuilder.beginControlFlow("($tempVariableName as? JsonApiModel)?.let")
        codeBlockBuilder.addStatement("$tempVariableName.setType(type)")
        codeBlockBuilder.addStatement("$tempVariableName.setId(id)")
        codeBlockBuilder.endControlFlow()

        codeBlockBuilder.addStatement("return $tempVariableName")

        return builder.addCode(codeBlockBuilder.build().toString()).build()
    }

    private fun idProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.ID,
        String::class,
        KModifier.OVERRIDE,
    ).addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.ID))
        .initializer(JsonApiConstants.Keys.ID)
        .build()

    private fun idParam(): ParameterSpec = ParameterSpec.builder(
        JsonApiConstants.Keys.ID,
        String::class,
    ).defaultValue("%S", "0")
        .build()

    private fun typeProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.TYPE,
        String::class,
        KModifier.OVERRIDE,
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.TYPE),
    ).initializer(JsonApiConstants.Keys.TYPE).build()

    private fun typeParam(type: String): ParameterSpec = ParameterSpec.builder(
        JsonApiConstants.Keys.TYPE,
        String::class,
    ).defaultValue("%S", type)
        .build()
}
