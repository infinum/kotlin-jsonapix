package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.AttributesModel
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.processor.ClassInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

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
        data: ClassName,
        wrapper: ClassName,
        resourceObject: ClassName,
        type: String,
        propertyNames: List<String>
    ) {
        specsMap[data] = ClassInfo(wrapper, resourceObject, type, propertyNames)
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
                "return %M.%M<%T<%T>>(jsonStringWithDiscriminator).data?.attributes?.toOriginalObject()",
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
        properties: List<String>
    ): FunSpec {
        return FunSpec.builder(MEMBER_WRAPPER_GETTER)
            .receiver(originalClass)
            .returns(wrapperClass)
            .addStatement(
                "return %T(%T_%T(%T_%T.fromOriginalObject(this)))",
                wrapperClass,
                ResourceObject::class.asClassName(),
                originalClass,
                AttributesModel::class.asClassName(),
                originalClass
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
                wrapperFunSpec(
                    it.key,
                    it.value.jsonWrapperClassName,
                    it.value.propertyNames
                )
            )
            fileSpec.addFunction(serializeFunSpec(it.key))
        }

        fileSpec.addProperty(jsonApiWrapperSerializerPropertySpec())
        fileSpec.addProperty(formatPropertySpec())
        fileSpec.addFunction(deserializeFunSpec())

        return fileSpec.build()
    }
}
