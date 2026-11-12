package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.findTypeMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.formatMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.jsonObjectMember
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.DeserializeFunSpecMemberProvider.typeOfMember
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

internal object DeserializeListFunSpecBuilder {

    @Suppress("LongMethod", "LongParameterList")
    fun build(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        val dataVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.DATA_TYPE_VARIABLE)

        val linksParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class).build(),
        )

        val metaParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_META, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_META, String::class).build(),
        )

        return FunSpec.builder(JsonApiConstants.Members.JSONX_LIST_DESERIALIZE)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
            .addTypeVariable(dataVariableName.copy(reified = true))
            .addTypeVariable(typeVariableName.copy(reified = true))
            .addParameters(linksParams)
            .addParameters(metaParams)
            .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.ERRORS, String::class).build())
            .returns(JsonApiXList::class.asClassName().parameterizedBy(dataVariableName, typeVariableName))
            .addStatement(
                "val de = %T.%L(this).%M[%S]",
                Json::class.asTypeName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT,
                jsonObjectMember,
                JsonApiConstants.Keys.DATA,
            )
            .addStatement(
                "val type = if((de as? kotlinx.serialization.json.JsonArray)?.size == 0) %M(Data::class) else %T.%M(de)",
                typeOfMember,
                TypeExtractor::class.asTypeName(),
                findTypeMember,
            )
            .addStatement(
                "val discriminator = %T(%L ?: %M(Model::class), %L, %L, %L, %L, %L, %L, %L)",
                JsonApiListDiscriminator::class,
                JsonApiConstants.Keys.TYPE,
                typeOfMember,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS,
            )
            .addStatement(
                "val jsonElement = %T.%L(this)",
                Json::class.asClassName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT,
            )
            .addStatement(
                "val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()",
            )
            // The serializer is passed explicitly, because only the JVM and JS can look up the serializer
            // of an interface at runtime. The discriminator picks the model's class, so the cast is safe.
            .addStatement(
                "return %M.%L(%T(%T::class), jsonStringWithDiscriminator) as %T<%T, %T>",
                formatMember,
                JsonApiConstants.Members.DECODE_FROM_STRING,
                PolymorphicSerializer::class,
                JsonApiXList::class,
                JsonApiXList::class,
                dataVariableName,
                typeVariableName,
            )
            .build()
    }
}
