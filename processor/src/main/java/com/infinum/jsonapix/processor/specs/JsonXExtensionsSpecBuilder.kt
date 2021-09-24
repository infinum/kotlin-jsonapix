package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.processor.ClassInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@SuppressWarnings("SpreadOperator")
internal class JsonXExtensionsSpecBuilder {

    private val specsMap = hashMapOf<ClassName, ClassInfo>()

    @SuppressWarnings("LongParameterList")
    fun add(
        type: String,
        data: ClassName,
        wrapper: ClassName,
        resourceObject: ClassName,
        attributesObject: ClassName?,
        relationshipsObject: ClassName?,
        includedListStatement: CodeBlock?
    ) {
        specsMap[data] = ClassInfo(
            type,
            wrapper,
            resourceObject,
            attributesObject,
            relationshipsObject,
            includedListStatement
        )
    }

    private fun deserializeFunSpec(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        val decodeMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
            JsonApiConstants.Members.DECODE_FROM_STRING
        )
        val formatMember = MemberName(
            JsonApiConstants.Packages.EXTENSIONS,
            JsonApiConstants.Members.FORMAT
        )
        val findTypeMember =
            MemberName(
                JsonApiConstants.Packages.TYPE_EXTRACTOR,
                JsonApiConstants.Members.FIND_TYPE
            )
        val jsonObjectMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_JSON,
            JsonApiConstants.Members.JSON_OBJECT
        )
        return FunSpec.builder(JsonApiConstants.Members.JSONX_DESERIALIZE)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .returns(typeVariableName.copy(nullable = true))
            .addStatement(
                "val type = %T.%M(%T.%L(this).%M[%S]!!)",
                TypeExtractor::class.asTypeName(),
                findTypeMember,
                Json::class.asTypeName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT,
                jsonObjectMember,
                JsonApiConstants.Keys.DATA
            )
            .addStatement("val discriminator = %T(type)", JsonApiDiscriminator::class)
            .addStatement(
                "val jsonElement = %T.%L(this)",
                Json::class.asClassName(),
                JsonApiConstants.Members.PARSE_TO_JSON_ELEMENT
            )
            .addStatement(
                "val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()"
            )
            .addStatement(
                "return %M.%M<%T<%T>>(jsonStringWithDiscriminator).${
                    JsonApiConstants.Members.ORIGINAL
                }",
                formatMember,
                decodeMember,
                JsonApiX::class,
                typeVariableName
            )
            .build()
    }

    private fun formatPropertySpec(): PropertySpec {
        val formatCodeBuilder = CodeBlock.builder()
            .addStatement("%T {", Json::class)
            .indent()
            .addStatement(JsonApiConstants.Statements.ENCODE_DEFAULTS)
            .addStatement(
                JsonApiConstants.Statements.CLASS_DISCRIMINATOR_FORMAT,
                JsonApiConstants.CLASS_DISCRIMINATOR_KEY
            )
            .addStatement(
                JsonApiConstants.Statements.SERIALIZERS_MODULE_FORMAT,
                JsonApiConstants.Members.JSONX_SERIALIZER_MODULE
            )
            .addStatement(JsonApiConstants.Statements.IGNORE_UNKNOWN_KEYS)
            .unindent()
            .addStatement("}")
        return PropertySpec.builder(JsonApiConstants.Members.FORMAT, Json::class)
            .initializer(formatCodeBuilder.build())
            .build()
    }

    @SuppressWarnings("LongMethod")
    private fun jsonApiWrapperSerializerPropertySpec(): PropertySpec {
        val codeBlockBuilder = CodeBlock.builder()
        val polymorpicMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.POLYMORPHIC
        )
        val subclassMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.SUBCLASS
        )
        val contextualMember = MemberName(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            JsonApiConstants.Members.CONTEXTUAL
        )
        codeBlockBuilder.addStatement("%T {", SerializersModule::class)
        codeBlockBuilder.indent()
            .addStatement("%M(%T::class) {", polymorpicMember, JsonApiX::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            codeBlockBuilder.addStatement("%M(%T::class)", subclassMember, it.jsonWrapperClassName)
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder
            .addStatement("%M(%T::class) {", polymorpicMember, ResourceObject::class)
        codeBlockBuilder.indent()
        specsMap.values.forEach {
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                it.resourceObjectClassName
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
                    it.attributesWrapperClassName
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
                    it.relationshipsObjectClassName
                )
            }
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            Links::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            OneRelationshipMember::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ManyRelationshipMember::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ResourceIdentifier::class.asClassName()
        )

        codeBlockBuilder.unindent().addStatement("}")

        return PropertySpec.builder(
            JsonApiConstants.Members.JSONX_SERIALIZER_MODULE,
            SerializersModule::class
        )
            .initializer(codeBlockBuilder.build()).build()
    }

    private fun serializeFunSpec(originalClass: ClassName): FunSpec {
        val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
        val jsonXClass = JsonApiX::class.asClassName()
        val formatMember = MemberName(
            JsonApiConstants.Packages.EXTENSIONS,
            JsonApiConstants.Members.FORMAT
        )
        val encodeMember =
            MemberName(
                JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
                JsonApiConstants.Members.ENCODE_TO_STRING
            )
        val jsonApiWrapperMember =
            MemberName(
                JsonApiConstants.Packages.EXTENSIONS,
                JsonApiConstants.Members.JSONX_WRAPPER_GETTER
            )
        return FunSpec.builder(JsonApiConstants.Members.JSONX_SERIALIZE)
            .receiver(originalClass)
            .returns(String::class)
            .addStatement("val jsonX = this.%M()", jsonApiWrapperMember)
            .addStatement(
                "val discriminator = %T(jsonX.data.type)",
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

    private fun wrapperFunSpec(
        originalClass: ClassName,
        wrapperClass: ClassName,
        attributesClass: ClassName?,
        relationshipsClass: ClassName?,
        includedListStatement: String?
    ): FunSpec {
        val builderArgs =
            mutableListOf<Any>(wrapperClass, ResourceObject::class.asClassName(), originalClass)
        val returnStatement = StringBuilder("return %T(%T_%T(")

        if (attributesClass != null) {
            builderArgs.add(attributesClass)
            returnStatement.append(
                "attributes = %T.${
                    JsonApiConstants.Members.FROM_ORIGINAL_OBJECT
                }(this)"
            )
        }

        if (relationshipsClass != null) {
            if (attributesClass != null) {
                returnStatement.append(", ")
            }
            returnStatement.append(
                "relationships = %T.${
                    JsonApiConstants.Members.FROM_ORIGINAL_OBJECT
                }(this)"
            )
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append(")")
        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_GETTER)
            .receiver(originalClass)
            .returns(wrapperClass)
            .addStatement(
                returnStatement.toString(),
                *builderArgs.toTypedArray()
            )
            .build()
    }

    private fun oneRelationshipModel(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        return FunSpec.builder(JsonApiConstants.Members.TO_ONE_RELATIONSHIP_MODEL)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .receiver(typeVariableName)
            .returns(OneRelationshipMember::class)
            .addParameter("type", String::class)
            .addParameter(ParameterSpec.builder("id", String::class).defaultValue("%S", "").build())
            .addStatement(
                "return %T(data = %T(type, id))",
                OneRelationshipMember::class.asClassName(),
                ResourceIdentifier::class.asClassName()
            )
            .build()
    }

    private fun manyRelationshipModel(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        return FunSpec.builder(JsonApiConstants.Members.TO_MANY_RELATIONSHIP_MODEL)
            .receiver(Collection::class.asClassName().parameterizedBy(typeVariableName))
            .returns(ManyRelationshipMember::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .addParameter("type", String::class)
            .addParameter(
                ParameterSpec.builder(
                    "idMapper", Function1::class.asClassName()
                        .parameterizedBy(typeVariableName, String::class.asClassName())
                ).defaultValue("{ \"\" }").build()
            )
            .addStatement(
                "return %T(data = map { %T(type, idMapper(it)) })",
                ManyRelationshipMember::class.asClassName(),
                ResourceIdentifier::class.asClassName()
            )
            .build()
    }

    private fun resourceObject(
        originalClass: ClassName,
        resourceObjectClass: ClassName,
        attributesClass: ClassName?,
        relationshipsClass: ClassName?
    ): FunSpec {
        val returnStatement = StringBuilder("return %T(")
        val builderArgs = mutableListOf<Any>(resourceObjectClass)

        if (attributesClass != null) {
            returnStatement.append(
                "attributes = %T.${
                    JsonApiConstants.Members.FROM_ORIGINAL_OBJECT
                }(this)"
            )
            builderArgs.add(attributesClass)
        }

        if (relationshipsClass != null) {
            if (attributesClass != null) {
                returnStatement.append(", ")
            }
            returnStatement.append(
                "relationships = %T.${
                    JsonApiConstants.Members.FROM_ORIGINAL_OBJECT
                }(this)"
            )
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append(")")

        return FunSpec.builder(JsonApiConstants.Members.TO_RESOURCE_OBJECT)
            .receiver(originalClass)
            .returns(resourceObjectClass)
            .addStatement(
                returnStatement.toString(),
                *builderArgs.toTypedArray()
            )
            .build()
    }

    @SuppressWarnings("SpreadOperator")
    fun build(): FileSpec {
        val fileSpec =
            FileSpec.builder(
                JsonApiConstants.Packages.EXTENSIONS,
                JsonApiConstants.FILE_NAME_EXTENSIONS
            )
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class)
                .addMember("%S", JsonApiConstants.FILE_NAME_EXTENSIONS)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build()
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
            *JsonApiConstants.Imports.KOTLINX
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_DISCRIMINATORS,
            *JsonApiConstants.Imports.CORE_EXTENSIONS
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            *JsonApiConstants.Imports.KOTLINX_MODULES
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.EXTENSIONS,
            *JsonApiConstants.Imports.JSON_X
        )

        specsMap.entries.forEach {
            fileSpec.addFunction(
                resourceObject(
                    it.key,
                    it.value.resourceObjectClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName
                )
            )
            fileSpec.addFunction(
                wrapperFunSpec(
                    it.key,
                    it.value.jsonWrapperClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName,
                    it.value.includedListStatement?.toString()
                )
            )
            fileSpec.addFunction(serializeFunSpec(it.key))
        }

        fileSpec.addProperty(jsonApiWrapperSerializerPropertySpec())
        fileSpec.addProperty(formatPropertySpec())
        fileSpec.addFunction(manyRelationshipModel())
        fileSpec.addFunction(oneRelationshipModel())
        fileSpec.addFunction(deserializeFunSpec())

        return fileSpec.build()
    }
}
