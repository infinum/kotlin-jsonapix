package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterListSpecBuilder : BaseTypeAdapterSpecBuilder() {
    override fun getAdapterPrefixName(): String = JsonApiConstants.Prefix.TYPE_ADAPTER_LIST

    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_LIST

    override fun getRootModel(className: ClassName): TypeName = List::class.asClassName().parameterizedBy(className)

    override fun getAdditionalImports(): List<String> =
        listOf(
            JsonApiConstants.Members.JSONX_SERIALIZE,
            JsonApiConstants.Members.JSONX_LIST_DESERIALIZE,
        )

    override fun convertFromStringFunSpec(
        className: ClassName,
        modelType: TypeName,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?,
    ): FunSpec =
        FunSpec
            .builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(modelType)
            .addStatement(
                "val data = input.%N<%T,%T>(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_LIST_DESERIALIZE,
                className,
                modelType,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS,
            ).addStatement("return data.%L", JsonApiConstants.Members.ORIGINAL)
            .build()
}
