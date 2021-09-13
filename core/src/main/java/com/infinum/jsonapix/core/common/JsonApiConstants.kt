package com.infinum.jsonapix.core.common

/**
 * Contains all the constants used in JSON:API specification
 * as well as prefixes for the generated classes
 */
object JsonApiConstants {

    const val CLASS_DISCRIMINATOR_KEY = "#class"
    const val SERIAL_NAME_FORMAT = "value = %S"

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
        const val JSONAPI = "JsonApi_"
        const val RESOURCE_OBJECT = "ResourceObject_"
        const val ATTRIBUTES = "Attributes_"
        const val RELATIONSHIPS = "Relationships_"
        const val RESOURCE_IDENTIFIER = "ResourceIdentifier_"

        fun String.withName(name: String): String = "$this$name"
    }
}