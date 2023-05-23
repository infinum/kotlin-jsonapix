package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.decodeMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.findTypeMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.formatMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.jsonObjectMember
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.json.Json

internal object DeserializeFunSpecBuilder {

    fun build(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)

        val linksParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class).build()
        )

        val metaParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_META, String::class).build()
        )

        return FunSpec.builder(JsonApiConstants.Members.JSONX_DESERIALIZE)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .addParameters(linksParams)
            .addParameters(metaParams)
            .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.ERRORS, String::class).build())
            .returns(JsonApiX::class.asClassName().parameterizedBy(typeVariableName))
            .addStatement(
                "val type = %T.%M(%T.%L(this).%M[%S])",
                TypeExtractor::class.asTypeName(),
                findTypeMember,
                Json::class.asTypeName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT,
                jsonObjectMember,
                JsonApiConstants.Keys.DATA
            )
            .addStatement(
                "val discriminator = %T(%L ?: TypeExtractor.guessType(Model::class), %L, %L, %L, %L, %L, %L, %L)",
                JsonApiDiscriminator::class,
                JsonApiConstants.Keys.TYPE,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS
            )
            .addStatement(
                "val jsonElement = %T.%L(this)",
                Json::class.asClassName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT
            )
            .addStatement(
                "val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()"
            )
            .addStatement(
                "return %M.%M<%T<%T>>(jsonStringWithDiscriminator)",
                formatMember,
                decodeMember,
                JsonApiX::class,
                typeVariableName
            )
            .build()
    }
}
