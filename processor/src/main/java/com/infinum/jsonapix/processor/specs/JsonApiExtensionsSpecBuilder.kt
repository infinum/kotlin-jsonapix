package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.AttributesModel
import com.infinum.jsonapix.core.resources.IncludedModel
import com.infinum.jsonapix.core.resources.LinksModel
import com.infinum.jsonapix.core.resources.ManyRelationshipMemberModel
import com.infinum.jsonapix.core.resources.OneRelationshipMemberModel
import com.infinum.jsonapix.core.resources.RelationshipsModel
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
import java.util.function.Function

internal class JsonApiExtensionsSpecBuilder {

    companion object {
        private const val PACKAGE_EXTENSIONS = "com.infinum.jsonapix"
        private const val PACKAGE_KOTLINX_SERIALIZATION = "kotlinx.serialization"
        private const val PACKAGE_KOTLINX_SERIALIZATION_MODULES = "kotlinx.serialization.modules"
        private const val PACKAGE_CORE_DISCRIMINATORS = "com.infinum.jsonapix.core.discriminators"
        private const val PACKAGE_KOTLINX_SERIALIZATION_JSON = "kotlinx.serialization.json"
        private const val PACKAGE_TYPE_EXTRACTOR =
            "com.infinum.jsonapix.core.discriminators.TypeExtractor"

        private const val FILE_NAME_EXTENSIONS = "JsonApiExtensions"

        private const val MEMBER_WRAPPER_GETTER = "toJsonApiWrapper"
        private const val MEMBER_SERIALIZER = "toJsonApiString"
        private const val MEMBER_DESERIALIZER = "decodeJsonApiString"
        private const val MEMBER_DECODE_FROM_STRING = "decodeFromString"
        private const val MEMBER_ENCODE_TO_STRING = "encodeToString"
        private const val MEMBER_FORMAT = "format"
        private const val MEMBER_GENERIC_TYPE_VARIABLE = "T"
        private const val MEMBER_FIND_TYPE = "findType"
        private const val MEMBER_PARSE_TO_JSON_ELEMENT = "parseToJsonElement"
        private const val MEMBER_SERIALIZERS_MODULE = "jsonApiSerializerModule"
        private const val MEMBER_POLYMORPHIC = "polymorphic"
        private const val MEMBER_SUBCLASS = "subclass"
        private const val MEMBER_JSON_OBJECT = "jsonObject"
        private const val MEMBER_CONTEXTUAL = "contextual"

        private const val CLASS_DISCRIMINATOR = "#class"
        private const val KEY_DATA = "data"

        private const val STATEMENT_ENCODE_DEFAULTS = "encodeDefaults = true"

        private val IMPORTS_JSON_API_WRAPPER = arrayOf(
            "core.JsonApiWrapper",
            "core.resources.ResourceObject"
        )

        private val IMPORTS_KOTLINX = arrayOf(
            "json.Json",
            "json.jsonObject",
            "encodeToString",
            "decodeFromString",
            "PolymorphicSerializer"
        )

        private val IMPORTS_KOTLINX_MODULES = arrayOf(
            "polymorphic",
            "contextual",
            "subclass",
            "SerializersModule"
        )

        private val IMPORTS_CORE_EXTENSIONS = arrayOf(
            "JsonApiDiscriminator",
            "TypeExtractor"
        )
    }

    private val specsMap = hashMapOf<ClassName, ClassInfo>()

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
        val typeVariableName = TypeVariableName.invoke(MEMBER_GENERIC_TYPE_VARIABLE)
        val decodeMember = MemberName(PACKAGE_KOTLINX_SERIALIZATION, MEMBER_DECODE_FROM_STRING)
        val formatMember = MemberName(PACKAGE_EXTENSIONS, MEMBER_FORMAT)
        val findTypeMember = MemberName(PACKAGE_TYPE_EXTRACTOR, MEMBER_FIND_TYPE)
        val jsonObjectMember = MemberName(PACKAGE_KOTLINX_SERIALIZATION_JSON, MEMBER_JSON_OBJECT)
        return FunSpec.builder(MEMBER_DESERIALIZER)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .returns(typeVariableName.copy(nullable = true))
            .addStatement(
                "val type = %T.%M(%T.%L(this).%M[%S]!!)",
                TypeExtractor::class.asTypeName(),
                findTypeMember,
                Json::class.asTypeName(),
                MEMBER_PARSE_TO_JSON_ELEMENT,
                jsonObjectMember,
                KEY_DATA
            )
            .addStatement("val discriminator = %T(type)", JsonApiDiscriminator::class)
            .addStatement(
                "val jsonElement = %T.%L(this)",
                Json::class.asClassName(),
                MEMBER_PARSE_TO_JSON_ELEMENT
            )
            .addStatement(
                "val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()"
            )
            .addStatement(
                "return %M.%M<%T<%T>>(jsonStringWithDiscriminator).data?.attributes",
                formatMember,
                decodeMember,
                JsonApiWrapper::class,
                typeVariableName
            )
            .build()
    }

    private fun formatPropertySpec(): PropertySpec {
        val formatCodeBuilder = CodeBlock.builder()
            .addStatement("%T {", Json::class)
            .indent()
            .addStatement(STATEMENT_ENCODE_DEFAULTS)
            .addStatement("classDiscriminator = %S", CLASS_DISCRIMINATOR)
            .addStatement("serializersModule = %L", MEMBER_SERIALIZERS_MODULE)
            .unindent()
            .addStatement("}")
        return PropertySpec.builder(MEMBER_FORMAT, Json::class)
            .initializer(formatCodeBuilder.build())
            .build()
    }

    private fun jsonApiWrapperSerializerPropertySpec(): PropertySpec {
        val codeBlockBuilder = CodeBlock.builder()
        val polymorpicMember = MemberName(
            PACKAGE_KOTLINX_SERIALIZATION_MODULES,
            MEMBER_POLYMORPHIC
        )
        val subclassMember = MemberName(
            PACKAGE_KOTLINX_SERIALIZATION_MODULES,
            MEMBER_SUBCLASS
        )
        val contextualMember = MemberName(
            PACKAGE_KOTLINX_SERIALIZATION_MODULES,
            MEMBER_CONTEXTUAL
        )
        codeBlockBuilder.addStatement("%T {", SerializersModule::class)
        codeBlockBuilder.indent()
            .addStatement("%M(%T::class) {", polymorpicMember, JsonApiWrapper::class)
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
            .addStatement("%M(%T::class) {", polymorpicMember, AttributesModel::class)
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
            .addStatement("%M(%T::class) {", polymorpicMember, RelationshipsModel::class)
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
            LinksModel::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            OneRelationshipMemberModel::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ManyRelationshipMemberModel::class.asClassName()
        )

        codeBlockBuilder.addStatement(
            "%M(%T.serializer())",
            contextualMember,
            ResourceIdentifier::class.asClassName()
        )

        codeBlockBuilder.unindent().addStatement("}")

        return PropertySpec.builder(MEMBER_SERIALIZERS_MODULE, SerializersModule::class)
            .initializer(codeBlockBuilder.build()).build()
    }

    private fun serializeFunSpec(originalClass: ClassName): FunSpec {
        val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
        val jsonApiWrapperClass = JsonApiWrapper::class.asClassName()
        val formatMember = MemberName(PACKAGE_EXTENSIONS, MEMBER_FORMAT)
        val encodeMember =
            MemberName(PACKAGE_KOTLINX_SERIALIZATION, MEMBER_ENCODE_TO_STRING)
        val jsonApiWrapperMember = MemberName(PACKAGE_EXTENSIONS, MEMBER_WRAPPER_GETTER)
        return FunSpec.builder(MEMBER_SERIALIZER)
            .receiver(originalClass)
            .returns(String::class)
            .addStatement("val jsonWrapper = this.%M()", jsonApiWrapperMember)
            .addStatement(
                "val discriminator = %T(jsonWrapper.data.type)",
                JsonApiDiscriminator::class.asClassName()
            )
            .addStatement(
                "val jsonString = %M.%M(%T(%T::class), jsonWrapper)",
                formatMember,
                encodeMember,
                polymorphicSerializerClass,
                jsonApiWrapperClass
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
            returnStatement.append("attributes = %T.fromOriginalObject(this)")
        }

        if (relationshipsClass != null) {
            if (attributesClass != null) {
                returnStatement.append(", ")
            }
            returnStatement.append("relationships = %T.fromOriginalObject(this)")
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append(")")
        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(MEMBER_WRAPPER_GETTER)
            .receiver(originalClass)
            .returns(wrapperClass)
            .addStatement(
                returnStatement.toString(),
                *builderArgs.toTypedArray()
            )
            .build()
    }

    private fun oneRelationshipModel(): FunSpec {
        val typeVariableName = TypeVariableName.invoke(MEMBER_GENERIC_TYPE_VARIABLE)
        return FunSpec.builder("toOneRelationshipModel")
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .receiver(typeVariableName)
            .returns(OneRelationshipMemberModel::class)
            .addParameter("type", String::class)
            .addParameter(ParameterSpec.builder("id", String::class).defaultValue("%S", "").build())
            .addStatement(
                "return %T(data = %T(type, id))",
                OneRelationshipMemberModel::class.asClassName(),
                ResourceIdentifier::class.asClassName()
            )
            .build()
    }

    private fun manyRelationshipModel(): FunSpec {
        val typeVariableName = TypeVariableName.invoke(MEMBER_GENERIC_TYPE_VARIABLE)
        return FunSpec.builder("toManyRelationshipModel")
            .receiver(Collection::class.asClassName().parameterizedBy(typeVariableName))
            .returns(ManyRelationshipMemberModel::class)
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
                ManyRelationshipMemberModel::class.asClassName(),
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
            returnStatement.append("attributes = %T.fromOriginalObject(this)")
            builderArgs.add(attributesClass)
        }

        if (relationshipsClass != null) {
            if (attributesClass != null) {
                returnStatement.append(", ")
            }
            returnStatement.append("relationships = %T.fromOriginalObject(this)")
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append(")")

        return FunSpec.builder("toResourceObject")
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
        val fileSpec = FileSpec.builder(PACKAGE_EXTENSIONS, FILE_NAME_EXTENSIONS)
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class).addMember("%S", FILE_NAME_EXTENSIONS)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build()
        )

        fileSpec.addImport(
            PACKAGE_KOTLINX_SERIALIZATION,
            *IMPORTS_KOTLINX
        )

        fileSpec.addImport(
            PACKAGE_CORE_DISCRIMINATORS,
            *IMPORTS_CORE_EXTENSIONS
        )

        fileSpec.addImport(
            PACKAGE_KOTLINX_SERIALIZATION_MODULES,
            *IMPORTS_KOTLINX_MODULES
        )

        fileSpec.addImport(PACKAGE_EXTENSIONS, *IMPORTS_JSON_API_WRAPPER)

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
        //fileSpec.addFunction(deserializeFunSpec())

        return fileSpec.build()
    }
}
