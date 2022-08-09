package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.processor.specs.jsonxextensions.providers.SerializeFunSpecMemberProvider.encodeMember
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.PolymorphicSerializer

internal object SerializeListFunSpecBuilder {

    fun build(rootPackage: String, originalClass: ClassName): FunSpec {
        val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
        val jsonXListClass = JsonApiXList::class.asClassName()

        val linksParams = listOf(
            ParameterSpec.builder(JsonApiConstants.Members.ROOT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, String::class).build(),
            ParameterSpec.builder(JsonApiConstants.Members.RELATIONSHIPS_LINKS, String::class).build()
        )
        val formatMember = MemberName(rootPackage, JsonApiConstants.Members.FORMAT)
        val jsonApiListWrapperMember = MemberName(rootPackage, JsonApiConstants.Members.JSONX_WRAPPER_LIST_GETTER)
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
            .addStatement("val jsonX = this.%M()", jsonApiListWrapperMember)
            .addStatement(
                "val discriminator = %T(jsonX.data.first().type, %N, %N, %N, %N)",
                JsonApiListDiscriminator::class.asClassName(),
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Keys.META
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
