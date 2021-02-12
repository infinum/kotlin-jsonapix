package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.processor.ClassInfo
import com.squareup.kotlinpoet.*
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

internal class JsonApiExtensionsSpecBuilder {

    companion object {
        private const val EXTENSIONS_PACKAGE = "com.infinum.jsonapix"
        private const val EXTENSIONS_FILE_NAME = "JsonApiExtensions"
        private const val CORE_EXTENSIONS_PACKAGE = "com.infinum.jsonapix.core.extensions"
        private const val WRAPPER_GETTER_FUNCTION_NAME = "toJsonApiWrapper"
        private const val SERIALIZER_EXTENSION_NAME = "toJsonApiString"
        private const val DESERIALIZER_EXTENSION_NAME = "decodeJsonApiString"
        private const val DECODE_FROM_STRING_MEMBER_NAME = "decodeFromString"
        private const val ENCODE_TO_STRING_MEMBER_NAME = "encodeToString"
        private const val FORMAT_PROPERTY_NAME = "format"
        private const val GENERIC_TYPE_VARIABLE_NAME = "T"
        private const val INJECT_CLASS_DISCRIMINATOR_FUNCTION_NAME = "injectClassDiscriminator"
        private const val EXTRACT_CLASS_DISCRIMINATOR_FUNCTION_NAME = "extractClassDiscriminator"
        private const val FIND_TYPE_FUNCTION_NAME = "findType"
        private const val SERIALIZERS_MODULE_PROPERTY_NAME = "jsonApiSerializerModule"
        private const val KOTLINX_SERIALIZATION_PACKAGE = "kotlinx.serialization"
        private const val KOTLINX_SERIALIZATION_MODULES_PACKAGE = "kotlinx.serialization.modules"
        private const val POLYMORPHIC_FUNCTION_NAME = "polymorphic"
        private const val SUBCLASS_FUNCTION_NAME = "subclass"
        private const val CLASS_DISCRIMINATOR = "#class"
        private const val ENCODE_DEFAULTS_STATEMENT = "encodeDefaults = true"

        private val JSON_API_WRAPPER_IMPORTS = arrayOf(
            "core.JsonApiWrapper",
            "core.resources.ResourceObject"
        )

        private val KOTLINX_IMPORTS = arrayOf(
            "json.Json",
            "encodeToString",
            "decodeFromString",
            "PolymorphicSerializer"
        )

        private val KOTLINX_MODULES_IMPORTS = arrayOf(
            "polymorphic",
            "subclass",
            "SerializersModule"
        )

        private val CORE_EXTENSIONS_IMPORTS = arrayOf(
            "extractClassDiscriminator",
            "injectClassDiscriminator",
            "findType"
        )
    }

    private val specsMap = hashMapOf<ClassName, ClassInfo>()

    fun add(data: ClassName, wrapper: ClassName, resourceObject: ClassName, type: String) {
        specsMap[data] = ClassInfo(wrapper, resourceObject, type)
    }

    private fun deserializeFunSpec(): FunSpec {
        val typeVariableName = TypeVariableName.invoke(GENERIC_TYPE_VARIABLE_NAME)
        val decodeMember = MemberName(KOTLINX_SERIALIZATION_PACKAGE, DECODE_FROM_STRING_MEMBER_NAME)
        val formatMember = MemberName(EXTENSIONS_PACKAGE, FORMAT_PROPERTY_NAME)
        return FunSpec.builder(DESERIALIZER_EXTENSION_NAME)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .returns(typeVariableName.copy(nullable = true))
            .addStatement(
                "return %M.%M<%T<%T>>(this.%L(%S, this.%L())).data?.attributes",
                formatMember,
                decodeMember,
                JsonApiWrapper::class,
                typeVariableName,
                INJECT_CLASS_DISCRIMINATOR_FUNCTION_NAME,
                CLASS_DISCRIMINATOR,
                FIND_TYPE_FUNCTION_NAME
            )
            .build()
    }

    private fun formatPropertySpec(): PropertySpec {
        val formatCodeBuilder = CodeBlock.builder()
            .addStatement("%T {", Json::class)
            .indent()
            .addStatement(ENCODE_DEFAULTS_STATEMENT)
            .addStatement("classDiscriminator = %S", CLASS_DISCRIMINATOR)
            .addStatement("serializersModule = %L", SERIALIZERS_MODULE_PROPERTY_NAME)
            .unindent()
            .addStatement("}")
        return PropertySpec.builder(FORMAT_PROPERTY_NAME, Json::class)
            .initializer(formatCodeBuilder.build())
            .build()
    }

    private fun jsonApiWrapperSerializerPropertySpec(): PropertySpec {
        val codeBlockBuilder = CodeBlock.builder()
        val polymorpicMember = MemberName(
            KOTLINX_SERIALIZATION_MODULES_PACKAGE,
            POLYMORPHIC_FUNCTION_NAME
        )
        val subclassMember = MemberName(
            KOTLINX_SERIALIZATION_MODULES_PACKAGE,
            SUBCLASS_FUNCTION_NAME
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

        return PropertySpec.builder(SERIALIZERS_MODULE_PROPERTY_NAME, SerializersModule::class)
            .initializer(codeBlockBuilder.build()).build()
    }

    @SuppressWarnings("SpreadOperator")
    fun build(): FileSpec {
        val fileSpec = FileSpec.builder(EXTENSIONS_PACKAGE, EXTENSIONS_FILE_NAME)
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class).addMember("%S", EXTENSIONS_FILE_NAME)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build()
        )

        fileSpec.addImport(
            KOTLINX_SERIALIZATION_PACKAGE,
            *KOTLINX_IMPORTS
        )

        fileSpec.addImport(
            CORE_EXTENSIONS_PACKAGE,
            *CORE_EXTENSIONS_IMPORTS
        )

        fileSpec.addImport(
            KOTLINX_SERIALIZATION_MODULES_PACKAGE,
            *KOTLINX_MODULES_IMPORTS
        )

        fileSpec.addImport(EXTENSIONS_PACKAGE, *JSON_API_WRAPPER_IMPORTS)

        specsMap.entries.forEach {
            fileSpec.addFunction(
                FunSpec.builder(WRAPPER_GETTER_FUNCTION_NAME)
                    .receiver(it.key)
                    .returns(it.value.jsonWrapperClassName)
                    .addStatement(
                        "return %T(%T_%T(this))",
                        it.value.jsonWrapperClassName,
                        ResourceObject::class.asClassName(),
                        it.key
                    )
                    .build()
            )

            val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
            val jsonApiWrapperClass = JsonApiWrapper::class.asClassName()
            val formatMember = MemberName(EXTENSIONS_PACKAGE, FORMAT_PROPERTY_NAME)
            val encodeMember = MemberName(KOTLINX_SERIALIZATION_PACKAGE, ENCODE_TO_STRING_MEMBER_NAME)
            fileSpec.addFunction(
                FunSpec.builder(SERIALIZER_EXTENSION_NAME)
                    .receiver(it.key)
                    .returns(String::class)
                    .addStatement(
                        "return %M.%M(%T(%T::class), this.%L()).%L(%S)",
                        formatMember,
                        encodeMember,
                        polymorphicSerializerClass,
                        jsonApiWrapperClass,
                        WRAPPER_GETTER_FUNCTION_NAME,
                        EXTRACT_CLASS_DISCRIMINATOR_FUNCTION_NAME,
                        CLASS_DISCRIMINATOR
                    )
                    .build()
            )
        }

        fileSpec.addProperty(jsonApiWrapperSerializerPropertySpec())
        fileSpec.addProperty(formatPropertySpec())
        fileSpec.addFunction(deserializeFunSpec())

        return fileSpec.build()
    }
}
