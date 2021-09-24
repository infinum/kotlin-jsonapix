package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.ANY
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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

internal object JsonApiXSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    fun build(
        className: ClassName,
        type: String,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = mutableListOf<PropertySpec>()
        val params = mutableListOf<ParameterSpec>()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                resourceObjectClassName
            ).build()
        )

        properties.add(dataProperty(resourceObjectClassName))

        params.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )
        properties.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.ERRORS,
                List::class.parameterizedBy(String::class)
                    .copy(nullable = true)
            ).defaultValue("%L", "null")
                .build()
        )

        properties.add(errorsProperty())

        return FileSpec.builder(className.packageName, generatedName)
            .addImport(
                JsonApiConstants.Packages.CORE_RESOURCES,
                JsonApiConstants.Imports.RESOURCE_IDENTIFIER
            )
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        JsonApiX::class.asClassName().parameterizedBy(className)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(Specs.getSerialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build()
                    )
                    .addProperties(properties)
                    .addProperty(
                        originalProperty(
                            className,
                            attributes,
                            oneRelationships,
                            manyRelationships
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun getAnnotatedAnyType(): TypeName {
        val contextual = AnnotationSpec.builder(Contextual::class).build()
        return ANY.copy(annotations = ANY.annotations + contextual)
    }

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.DATA, resourceObject
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA)
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.ERRORS,
        List::class.parameterizedBy(String::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.ERRORS))
        .initializer(JsonApiConstants.Keys.ERRORS)
        .build()

    private fun originalProperty(
        className: ClassName,
        attributes: List<PropertySpec>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>
    ): PropertySpec {
        var codeString = "${className.simpleName}("
        val builder = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            className, KModifier.OVERRIDE
        )
        attributes.forEach {
            codeString += "${it.name} = data.attributes?.${it.name}"
            if (!it.type.isNullable) {
                codeString += "!!"
            }
            codeString += ", "
        }
        val typeParams = mutableListOf<TypeName>()
        oneRelationships.forEach {
            codeString += "${it.key} = included?.firstOrNull { it.type == data.relationships?.${
                it.key
            }?.data?.type && it.id == data.relationships.${
                it.key
            }.data.id }?.${
                JsonApiConstants.Members.GET_ORIGINAL_OR_NULL
            }() as %T, "
            typeParams.add(it.value)
        }
        manyRelationships.forEach {
            codeString += "${it.key} = included?.filter { data.relationships?.${
                it.key
            }?.data?.contains(ResourceIdentifier(it.type, it.id)) == true }?.map { it.${
                JsonApiConstants.Members.GET_ORIGINAL_OR_NULL
            }() } as %T, "
            typeParams.add(it.value)
        }
        codeString += ")"
        return builder.initializer(codeString, *typeParams.toTypedArray()).build()
    }
}
