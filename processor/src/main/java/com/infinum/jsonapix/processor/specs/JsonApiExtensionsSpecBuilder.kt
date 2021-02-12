package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.*
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

internal class JsonApiExtensionsSpecBuilder {

    companion object {
        private const val EXTENSIONS_PACKAGE = "com.infinum.jsonapix"
        private const val EXTENSIONS_FILE_NAME = "JsonApiExtensions"
        private const val WRAPPER_GETTER_FUNCTION_NAME = "toJsonApiWrapper"
        private const val SERIALIZER_EXTENSION_NAME = "toJsonApiString"
        private const val DESERIALIZER_EXTENSION_NAME = "decodeJsonApiString"
        private const val FORMAT_PROPERTY_NAME = "format"
        private const val SERIALIZERS_MODULE_PROPERTY_NAME = "jsonApiSerializerModule"
        private const val KOTLINX_SERIALIZATION_PACKAGE = "kotlinx.serialization"
        private const val KOTLINX_SERIALIZATION_MODULES_PACKAGE = "kotlinx.serialization.modules"
        private const val POLYMORPHIC_FUNCTION_NAME = "polymorphic"
        private const val SUBCLASS_FUNCTION_NAME = "subclass"
        private const val CLASS_DISCRIMINATOR = "#class"

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
    }

    private val specsMap = hashMapOf<ClassName, Pair<ClassName, String>>()

    fun add(data: ClassName, wrapper: ClassName, type: String) {
        specsMap[data] = Pair(wrapper, type)
    }

    private fun deserializeFunSpec(): FunSpec {
        val typeVariableName = TypeVariableName.invoke("T")
        val decodeMember = MemberName(KOTLINX_SERIALIZATION_PACKAGE, "decodeFromString")
        return FunSpec.builder(DESERIALIZER_EXTENSION_NAME)
            .receiver(String::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .returns(typeVariableName.copy(nullable = true))
            .addStatement(
                "return format.%M<%T<%T>>(this.%L(%S, this.%L())).data?.attributes",
                decodeMember,
                JsonApiWrapper::class,
                typeVariableName,
                "injectClassDiscriminator",
                CLASS_DISCRIMINATOR,
                "findType"
            )
            .build()
    }

    private fun formatPropertySpec(): PropertySpec {
        val formatCodeBuilder = CodeBlock.builder()
            .addStatement("%T {", Json::class)
            .indent()
            .addStatement("encodeDefaults = true")
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
            codeBlockBuilder.addStatement("%M(%T::class)", subclassMember, it.first)
        }
        codeBlockBuilder.unindent().addStatement("}")

        codeBlockBuilder.indent()
            .addStatement("%M(%T::class) {", polymorpicMember, ResourceObject::class)
        codeBlockBuilder.indent()
        specsMap.keys.forEach {
            // TODO FIX ME. Make collector smarter to find both JsonApiWrapper and ResourceObject implementations
            codeBlockBuilder.addStatement(
                "%M(%T::class)",
                subclassMember,
                ClassName("com.infinum.jsonapix", "ResourceObject_${it.simpleName}")
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
            "com.infinum.jsonapix.core.extensions",
            "extractClassDiscriminator",
            "injectClassDiscriminator",
            "findType"
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
                    .returns(it.value.first)
                    .addStatement(
                        "return %T(%T_%T(this))",
                        it.value.first,
                        ResourceObject::class.asClassName(),
                        it.key
                    )
                    .build()
            )

            val polymorphicSerializerClass = PolymorphicSerializer::class.asClassName()
            val jsonApiWrapperClass = JsonApiWrapper::class.asClassName()
            fileSpec.addFunction(
                FunSpec.builder(SERIALIZER_EXTENSION_NAME)
                    .receiver(it.key)
                    .returns(String::class)
                    .addStatement(
                        "return format.encodeToString(%T(%T::class), this.%L()).%L(%S)",
                        polymorphicSerializerClass,
                        jsonApiWrapperClass,
                        WRAPPER_GETTER_FUNCTION_NAME,
                        "extractClassDiscriminator",
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
