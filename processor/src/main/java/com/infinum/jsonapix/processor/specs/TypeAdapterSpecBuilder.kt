package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Meta
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterSpecBuilder : BaseTypeAdapterSpecBuilder() {
    override fun getAdapterPrefixName(): String = JsonApiConstants.Prefix.TYPE_ADAPTER
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_MODEL

    override fun getRootModel(className: ClassName): TypeName = className

    override fun getAdditionalImports(): List<String> {
        return listOf(
            JsonApiConstants.Members.JSONX_SERIALIZE,
            JsonApiConstants.Members.JSONX_DESERIALIZE,
        )
    }

    override fun convertFromStringFunSpec(
        className: ClassName,
        modelType: TypeName,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?
    ): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(modelType)
            .addStatement(
                "val data = input.%N<%T>(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_DESERIALIZE,
                getRootModel(className),
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
                getClassSuffixName(),
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
                relationshipsMeta ?: Meta::class.asClassName()
            )
            .build()
    }
}