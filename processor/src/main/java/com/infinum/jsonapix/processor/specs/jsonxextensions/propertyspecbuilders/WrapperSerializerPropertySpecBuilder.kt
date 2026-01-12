package com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.resources.UnknownMeta
import com.infinum.jsonapix.processor.ClassInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.modules.SerializersModule

internal object WrapperSerializerPropertySpecBuilder {

    @Suppress("LongMethod", "StringLiteralDuplication")
    fun build(
        specsMap: HashMap<ClassName, ClassInfo>,
        customLinks: List<ClassName>,
        customErrors: Map<String, ClassName>,
        metas: List<ClassName>,
    ): PropertySpec {
        val codeBlockBuilder = CodeBlock.builder()
        val polymorpicMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.POLYMORPHIC,
        )
        val subclassMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.SUBCLASS,
        )

        val contextualMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.CONTEXTUAL,
        )
        codeBlockBuilder.addStatement("%T {", SerializersModule::class)
        codeBlockBuilder.indent()
            .addStatement("%M(%T::class) {", polymorpicMember, JsonApiX::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            codeBlockBuilder.addStatement("%M(%T::class)", subclassMember, it.jsonWrapperClassName)
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement("%M(%T::class) {", polymorpicMember, JsonApiXList::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            codeBlockBuilder.addStatement("%M(%T::class)", subclassMember, it.jsonWrapperListClassName)
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder
            .addStatement("%M(%T::class) {", polymorpicMember, ResourceObject::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                it.resourceObjectClassName,
            )
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder
            .addStatement("%M(%T::class) {", polymorpicMember, Attributes::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            if (it.attributesWrapperClassName != null) {
                codeBlockBuilder.addStatement(
                    "%M(%T::class)",
                    subclassMember,
                    it.attributesWrapperClassName,
                )
            }
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder
            .addStatement("%M(%T::class) {", polymorpicMember, Relationships::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            if (it.relationshipsObjectClassName != null) {
                codeBlockBuilder.addStatement(
                    "%M(%T::class)",
                    subclassMember,
                    it.relationshipsObjectClassName,
                )
            }
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement(
            "%M(%T::class) {",
            polymorpicMember,
            Links::class.asClassName(),
        )

        codeBlockBuilder.indent()

        codeBlockBuilder.addStatement(
            "%M(%T::class)",
            subclassMember,
            DefaultLinks::class.asClassName(),
        )

        customLinks.forEach { link ->
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                link,
            )
        }

        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement(
            "%M(%T::class) {",
            polymorpicMember,
            Error::class.asClassName(),
        )

        codeBlockBuilder.indent()

        codeBlockBuilder.addStatement(
            "%M(%T::class)",
            subclassMember,
            DefaultError::class.asClassName(),
        )

        customErrors.forEach { error ->
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                error.value,
            )
        }

        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement(
            "%M(%T::class) {",
            polymorpicMember,
            Meta::class.asClassName(),
        )

        codeBlockBuilder.indent()

        metas.forEach { meta ->
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                meta,
            )
        }

        codeBlockBuilder.addStatement(
            "defaultDeserializer{ %T.serializer() }",
            UnknownMeta::class.asClassName(),
        )

        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            OneRelationshipMember::class.asClassName(),
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ManyRelationshipMember::class.asClassName(),
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ResourceIdentifier::class.asClassName(),
        )

        codeBlockBuilder.unindent().addStatement("}")

        return PropertySpec.builder(
            JsonApiConstants.Members.JSONX_SERIALIZER_MODULE,
            SerializersModule::class,
        )
            .initializer(codeBlockBuilder.build()).build()
    }
}
