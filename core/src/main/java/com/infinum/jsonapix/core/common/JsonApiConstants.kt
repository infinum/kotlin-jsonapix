package com.infinum.jsonapix.core.common

/**
 * Contains all the constants used in JSON:API specification
 * as well as prefixes for the generated classes
 */
object JsonApiConstants {

    const val CLASS_DISCRIMINATOR_KEY = "#class"
    const val SERIAL_NAME_FORMAT = "value = %S"
    const val FILE_NAME_EXTENSIONS = "JsonXExtensions"
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

    object Keys {
        const val DATA = "data"
        const val ATTRIBUTES = "attributes"
        const val RELATIONSHIPS = "relationships"
        const val INCLUDED = "included"
        const val TYPE = "type"
        const val ID = "id"
        const val LINKS = "links"
        const val ERRORS = "errors"
    }

    object Prefix {
        const val JSON_API_X = "JsonApiX_"
        const val JSON_API_X_LIST = "JsonApiXList_"
        const val RESOURCE_OBJECT = "ResourceObject_"
        const val ATTRIBUTES = "Attributes_"
        const val RELATIONSHIPS = "Relationships_"

        fun String.withName(name: String): String = "$this$name"
    }

    object Members {
        const val FROM_ORIGINAL_OBJECT = "fromOriginalObject"
        const val TO_RESOURCE_OBJECT = "toResourceObject"
        const val ORIGINAL = "original"
        const val POLYMORPHIC = "polymorphic"
        const val SUBCLASS = "subclass"
        const val JSON_OBJECT = "jsonObject"
        const val CONTEXTUAL = "contextual"

        const val JSONX_WRAPPER_GETTER = "toJsonApiX"
        const val JSONX_WRAPPER_LIST_GETTER = "toJsonApiXList"
        const val JSONX_SERIALIZE = "toJsonApiXString"
        const val JSONX_DESERIALIZE = "decodeJsonApiXString"
        const val DECODE_FROM_STRING = "decodeFromString"
        const val ENCODE_TO_STRING = "encodeToString"
        const val FORMAT = "format"
        const val GENERIC_TYPE_VARIABLE = "Model"
        const val FIND_TYPE = "findType"
        const val PARSE_TO_JSON_ELEMENT = "parseToJsonElement"
        const val JSONX_SERIALIZER_MODULE = "jsonApiXSerializerModule"

        const val TO_ONE_RELATIONSHIP_MODEL = "toOneRelationshipModel"
        const val TO_MANY_RELATIONSHIP_MODEL = "toManyRelationshipModel"
    }

    object Packages {
        const val EXTENSIONS = "com.infinum.jsonapix"
        const val KOTLINX_SERIALIZATION = "kotlinx.serialization"
        const val KOTLINX_SERIALIZATION_MODULES = "kotlinx.serialization.modules"
        const val CORE_DISCRIMINATORS = "com.infinum.jsonapix.core.discriminators"
        const val KOTLINX_SERIALIZATION_JSON = "kotlinx.serialization.json"
        const val TYPE_EXTRACTOR = "com.infinum.jsonapix.core.discriminators.TypeExtractor"
        const val CORE_RESOURCES = "com.infinum.jsonapix.core.resources"
    }

    object Imports {
        val JSON_X = arrayOf(
            "core.JsonApiX",
            "core.resources.ResourceObject"
        )

        val KOTLINX = arrayOf(
            "json.Json",
            "json.jsonObject",
            "encodeToString",
            "decodeFromString",
            "PolymorphicSerializer"
        )

        val KOTLINX_MODULES = arrayOf(
            "polymorphic",
            "contextual",
            "subclass",
            "SerializersModule"
        )

        val CORE_EXTENSIONS = arrayOf(
            "JsonApiDiscriminator",
            "TypeExtractor"
        )

        const val RESOURCE_IDENTIFIER = "ResourceIdentifier"
    }

    object Statements {
        const val ENCODE_DEFAULTS = "encodeDefaults = true"
        const val IGNORE_UNKNOWN_KEYS = "ignoreUnknownKeys = true"
        const val CLASS_DISCRIMINATOR_FORMAT = "classDiscriminator = %S"
        const val SERIALIZERS_MODULE_FORMAT = "serializersModule = %L"
        const val RETURN_NULL = "return null"
    }
}
