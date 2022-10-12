package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import kotlinx.serialization.json.*
import kotlin.reflect.KAnnotatedElement

/**
 * Helper object that extracts the type string value from either json object or array.
 * In case of arrays, it extracts the type of the first array element.
 * Only use with JSON Arrays that contain objects of the same type
 */
object TypeExtractor {
    fun guessType(clazz: KAnnotatedElement): String {
        return (clazz.annotations.first { it is JsonApiX } as? JsonApiX)?.type!!
    }

    @SuppressWarnings("SwallowedException")
    fun findType(jsonElement: JsonElement): String {
        return try {
            when (jsonElement) {
                is JsonObject -> {
                    val type = jsonElement.jsonObject[JsonApiConstants.Keys.TYPE]
                    type?.jsonPrimitive?.content!!
                }
                is JsonArray -> {
                    val first = jsonElement.jsonArray[0]
                    val type = first.jsonObject[JsonApiConstants.Keys.TYPE]
                    type?.jsonPrimitive?.content!!
                }
                else -> {
                    throw IllegalArgumentException(
                        "Input must be either JSON object or array with the key type defined"
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            // TODO Add Timber and custom exceptions
            throw IllegalArgumentException(
                "Input must be either JSON object or array with the key type defined",
                e.cause
            )
        }
    }
}
