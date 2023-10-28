package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Extracts and injects [discriminator] from a [JsonElement].
 * Used to remove redundant class discriminator keys from output strings and inject the same
 * key-value pair before the deserialization.
 */
class CommonDiscriminator(private val discriminator: String) : Discriminator {

    private val discriminatorEntry = object : Map.Entry<String, JsonElement> {
        override val key = JsonApiConstants.CLASS_DISCRIMINATOR_KEY
        override val value = JsonPrimitive(discriminator)
    }

    override fun inject(jsonElement: JsonElement): JsonElement {
        return when (jsonElement) {
            is JsonObject -> {
                addDiscriminatorEntry(jsonElement)
            }

            is JsonArray -> {
                val jsonArray = jsonElement.jsonArray
                val newJsonArray = mutableListOf<JsonObject>()
                jsonArray.forEach {
                    val newJsonObject = addDiscriminatorEntry(it.jsonObject)
                    newJsonArray.add(newJsonObject)
                }
                JsonArray(newJsonArray)
            }

            else -> {
                throw IllegalArgumentException("Input must be either JSON object or array")
            }
        }
    }

    override fun extract(jsonElement: JsonElement): JsonElement {
        return when (jsonElement) {
            is JsonObject -> {
                removeDiscriminatorEntry(jsonElement)
            }

            is JsonArray -> {
                val jsonArray = jsonElement.jsonArray
                val newJsonArray = mutableListOf<JsonObject>()
                jsonArray.forEach {
                    val newJsonObject = removeDiscriminatorEntry(it.jsonObject)
                    newJsonArray.add(newJsonObject)
                }
                JsonArray(newJsonArray)
            }

            else -> {
                throw IllegalArgumentException("Input must be either JSON object or array")
            }
        }
    }

    private fun addDiscriminatorEntry(jsonObject: JsonObject): JsonObject {
        return jsonObject.entries
            .toMutableSet()
            .let { entries ->
                entries.add(discriminatorEntry)
                val resultMap = mutableMapOf<String, JsonElement>()
                resultMap.putAll(entries.map { Pair(it.key, it.value) })
                JsonObject(resultMap)
            }
    }

    private fun removeDiscriminatorEntry(jsonObject: JsonObject): JsonObject {
        return jsonObject.entries
            .toMutableList()
            .let { entries ->

                entries.removeAll { it.key == JsonApiConstants.CLASS_DISCRIMINATOR_KEY }

                entries.withIndex().firstOrNull { it.value.key == JsonApiConstants.Keys.META }?.let {
                    // Remove nested discriminator inside meta element

                    val metaJsonObject = it.value.value.takeUnless { json-> json is JsonNull }?.jsonObject
                    if (metaJsonObject != null) {
                        val meta = removeDiscriminatorEntry(metaJsonObject)
                        entries[it.index] = mapOf(JsonApiConstants.Keys.META to meta).entries.first()
                    }
                }

                val resultMap = mutableMapOf<String, JsonElement>()
                resultMap.putAll(entries.map { Pair(it.key, it.value) })
                JsonObject(resultMap)
            }
    }


}
