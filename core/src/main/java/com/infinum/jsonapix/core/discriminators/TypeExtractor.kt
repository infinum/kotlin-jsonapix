package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object TypeExtractor {

    private const val TYPE_KEY = "type"

    fun findType(jsonElement: JsonElement): String {
        return try {
            when (jsonElement) {
                is JsonObject -> {
                    val type = jsonElement.jsonObject[TYPE_KEY]
                    type?.jsonPrimitive?.content!!
                }
                is JsonArray -> {
                    val first = jsonElement.jsonArray[0]
                    val type = first.jsonObject[TYPE_KEY]
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