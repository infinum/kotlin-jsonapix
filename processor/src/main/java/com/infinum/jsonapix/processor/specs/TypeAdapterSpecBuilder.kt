package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXModel
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.Meta
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterSpecBuilder {

    public fun build(
        className: ClassName,
        rootLinks: String?,
        resourceObjectLinks: String?,
        relationshipsLinks: String?,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?,
        errors: String?
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.TYPE_ADAPTER.withName(className.simpleName)
        val typeAdapterClassName = ClassName(
            className.packageName,
            generatedName
        )
        val modelType = ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL))
        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(typeAdapterClassName)
                    .addSuperinterface(TypeAdapter::class.asClassName().parameterizedBy(modelType))
                    .addFunction(convertToStringFunSpec(className,modelType))
                    .addFunction(convertFromStringFunSpec(className,modelType,rootMeta,resourceObjectMeta,relationshipsMeta))
                    .apply {
                        if (rootLinks != null) {
                            addFunction(linksFunSpec(JsonApiConstants.Members.ROOT_LINKS, rootLinks))
                        }
                        if (resourceObjectLinks != null) {
                            addFunction(
                                linksFunSpec(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, resourceObjectLinks)
                            )
                        }
                        if (relationshipsLinks != null) {
                            addFunction(linksFunSpec(JsonApiConstants.Members.RELATIONSHIPS_LINKS, relationshipsLinks))
                        }

                        if (rootMeta != null) {
                            addFunction(metaFunSpec(JsonApiConstants.Members.ROOT_META, rootMeta.canonicalName))
                        }
                        if (resourceObjectMeta != null) {
                            addFunction(
                                metaFunSpec(JsonApiConstants.Members.RESOURCE_OBJECT_META, resourceObjectMeta.canonicalName)
                            )
                        }
                        if (relationshipsMeta != null) {
                            addFunction(metaFunSpec(JsonApiConstants.Members.RELATIONSHIPS_META, relationshipsMeta.canonicalName))
                        }

                        if (errors != null) {
                            addFunction(errorsFunSpec(errors))
                        }
                    }
                    .build()
            )
            .addImport(
                JsonApiConstants.Packages.JSONX,
                JsonApiConstants.Members.JSONX_SERIALIZE,
                JsonApiConstants.Members.JSONX_DESERIALIZE
            )
            .build()
    }

    private fun convertToStringFunSpec(className: ClassName,modelType: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_TO_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", modelType)
            .returns(String::class)
            .addStatement(
                """return ""  """
//                "return input.%N(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
//                JsonApiConstants.Members.JSONX_SERIALIZE,
//                JsonApiConstants.Members.ROOT_LINKS,
//                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
//                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
//                JsonApiConstants.Members.ROOT_META,
//                JsonApiConstants.Members.RESOURCE_OBJECT_META,
//                JsonApiConstants.Members.RELATIONSHIPS_META,
//                JsonApiConstants.Keys.ERRORS
            )
            .build()
    }

    private fun convertFromStringFunSpec(
        className: ClassName,
        modelType: ClassName,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?,
        ): FunSpec {

        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(modelType)
            .addStatement(
                "val data = input.%N<%T>(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_DESERIALIZE,
                className,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS
            )
            .addStatement(
                "return %N%N(%L, %L, %L, %L, %L, %L, %L, %L as %T, %L as %T, %L as %T})",
                className.simpleName,
                JsonApiConstants.Suffix.JSON_API_MODEL,
                "data.original",
                "data.data?.type",
                "data.data?.id",
                "data.links",
                "data.data?.links",
                "data.data?.relationshipsLinks()",
                "data.errors",
                "data.meta",
                rootMeta ?: Meta::class.asClassName(),
                "data.data?.meta",
                resourceObjectMeta ?: Meta::class.asClassName(),
                "data.data?.relationshipsMeta()?.mapValues { it.value",
                relationshipsMeta ?:  Meta::class.asClassName()
            )
            .build()
    }

    private fun linksFunSpec(methodName: String, links: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", links)
            .build()
    }

    private fun errorsFunSpec(errors: String): FunSpec {
        return FunSpec.builder(JsonApiConstants.Keys.ERRORS)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", errors)
            .build()
    }

    private fun metaFunSpec(methodName: String, meta: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", meta)
            .build()
    }
}
