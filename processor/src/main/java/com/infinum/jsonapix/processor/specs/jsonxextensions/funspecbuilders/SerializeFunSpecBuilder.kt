package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.encodeMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.formatMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.jsonApiWrapperMember
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.PolymorphicSerializer

internal object SerializeFunSpecBuilder {

    fun build(originalClass: ClassName, isNullable: Boolean): FunSpec {
        val modelClass = ClassName.bestGuess(originalClass.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL))

        val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
        val jsonXClass = JsonApiX::class.asClassName()

        val linksParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class)
                .build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class)
                .build()
        )

        val metaParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_META, String::class).build()
        )

        return FunSpec.builder(JsonApiConstants.Members.JSONX_SERIALIZE)
            .receiver(modelClass)
            .addParameters(linksParams)
            .addParameters(metaParams)
            .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.ERRORS, String::class).build())
            .returns(String::class)
            .addStatement("val jsonX = this.%M()", jsonApiWrapperMember)
            .addStatement(
                if (isNullable) "val type = jsonX.data?.type ?: TypeExtractor.guessType(this::class)" else "val type = jsonX.data.type"
            )
            .addStatement(
                "val discriminator = %T(type, %L, %L, %L, %L, %L, %L, %L)",
                JsonApiDiscriminator::class.asClassName(),
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS
            )
            .addStatement(
                "val jsonString = %M.%M(%T(%T::class), jsonX)",
                formatMember,
                encodeMember,
                polymorphicSerializerClass,
                jsonXClass
            )
            .addStatement(
                "return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()"
            )
            .build()
    }
}
