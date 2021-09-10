package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Helper object that extracts the type string value from either json object or array.
 * In case of arrays, it extracts the type of the first array element.
 * Only use with JSON Arrays that contain objects of the same type
 */
object TypeExtractor {

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
                    throw IllegalArgumentException()
                }
            }
        } catch (e: IllegalArgumentException) {
            throw e
        }
    }
}
