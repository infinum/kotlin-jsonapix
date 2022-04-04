package com.infinum.jsonapix.processor.specs.serializers

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.PolymorphicSerializer

@Suppress("LongMethod")
internal class FunSpecSerializer {

    companion object {

        fun serialize(originalClass: ClassName): FunSpec {
            val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
            val jsonXClass = JsonApiX::class.asClassName()
            val formatMember = MemberName(
                JsonApiConstants.Packages.JSONX,
                JsonApiConstants.Members.FORMAT
            )
            val encodeMember =
                MemberName(
                    JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
                    JsonApiConstants.Members.ENCODE_TO_STRING
                )
            val jsonApiWrapperMember =
                MemberName(
                    JsonApiConstants.Packages.JSONX,
                    JsonApiConstants.Members.JSONX_WRAPPER_GETTER
                )
            val linksParams = listOf(
                ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
                ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class).build(),
                ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class).build()
            )
            return FunSpec.builder(JsonApiConstants.Members.JSONX_SERIALIZE)
                .receiver(originalClass)
                .addParameters(linksParams)
                .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.META, String::class).build())
                .returns(String::class)
                .addStatement("val jsonX = this.%M()", jsonApiWrapperMember)
                .addStatement(
                    "val discriminator = %T(jsonX.data.type, ${JsonApiConstants.Members.ROOT_LINKS}, ${JsonApiConstants.Members.RESOURCE_OBJECT_LINKS}, ${JsonApiConstants.Members.RELATIONSHIPS_LINKS}, ${JsonApiConstants.Keys.META})",
                    JsonApiDiscriminator::class.asClassName()
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

        fun serializeList(originalClass: ClassName): FunSpec {
            val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
            val jsonXListClass = JsonApiXList::class.asClassName()
            val formatMember = MemberName(
                JsonApiConstants.Packages.JSONX,
                JsonApiConstants.Members.FORMAT
            )
            val encodeMember =
                MemberName(
                    JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
                    JsonApiConstants.Members.ENCODE_TO_STRING
                )
            val jsonApiWrapperMember =
                MemberName(
                    JsonApiConstants.Packages.JSONX,
                    JsonApiConstants.Members.JSONX_WRAPPER_LIST_GETTER
                )
            val linksParams = listOf(
                ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
                ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class).build(),
                ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class).build()
            )
            return FunSpec.builder(JsonApiConstants.Members.JSONX_SERIALIZE)
                .receiver(Iterable::class.asClassName().parameterizedBy(originalClass))
                .addParameters(linksParams)
                .addParameter(ParameterSpec.builder(JsonApiConstants.Keys.META, String::class).build())
                .returns(String::class)
                .addAnnotation(
                    AnnotationSpec.builder(JvmName::class)
                        .addMember("%S", "${JsonApiConstants.Members.JSONX_SERIALIZE}${originalClass.simpleName}")
                        .build()
                )
                .addStatement("val jsonX = this.%M()", jsonApiWrapperMember)
                .addStatement(
                    "val discriminator = %T(jsonX.data.first().type, ${JsonApiConstants.Members.ROOT_LINKS}, ${JsonApiConstants.Members.RESOURCE_OBJECT_LINKS}, ${JsonApiConstants.Members.RELATIONSHIPS_LINKS}, ${JsonApiConstants.Keys.META})",
                    JsonApiListDiscriminator::class.asClassName()
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
}
