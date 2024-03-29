package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.encodeMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.formatMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.jsonApiListWrapperMember
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.PolymorphicSerializer

internal object SerializeListFunSpecBuilder {

    fun build(originalClass: ClassName): FunSpec {
        val modelClass = ClassName.bestGuess(originalClass.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST))

        val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
        val jsonXListClass = JsonApiXList::class.asClassName()

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

        return FunSpec.builder(JsonApiConstants.Members.JSONX_SERIALIZE)
            .receiver(modelClass)
            .addParameters(linksParams)
            .addParameters(metaParams)
            .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.ERRORS, String::class).build())
            .returns(String::class)
            .addAnnotation(
                AnnotationSpec.builder(JvmName::class)
                    .addMember("%S", "${JsonApiConstants.Members.JSONX_SERIALIZE}${originalClass.simpleName}")
                    .build()
            )
            .addStatement("val jsonX = this.%M()", jsonApiListWrapperMember)
            .addStatement(
                "val type = jsonX.data.firstOrNull()?.type ?: %T.guessType(%T::class)",
                TypeExtractor::class.asTypeName(),
                originalClass,
            )
            .addStatement(
                "val discriminator = %T(type, %N, %N, %N, %N, %N, %N, %N)",
                JsonApiListDiscriminator::class.asClassName(),
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
                jsonXListClass
            )
            .addStatement(
                "return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()"
            )
            .build()
    }
}
