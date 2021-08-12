@file:SuppressWarnings("TooGenericExceptionCaught")

package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * This Discriminator is made specifically to handle JSON API objects. It leverages the functionality
 * of [CommonDiscriminator] and handles the whole hierarchy of JSON API object.
 */
class JsonApiDiscriminator(discriminator: String) : Discriminator {

    companion object {
        private const val DATA_KEY = "data"
        private const val INCLUDED_KEY = "included"
    }

    private val commonDiscriminator = CommonDiscriminator(discriminator)

    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val newIncludedArray = buildJsonArray {
                includedObject?.jsonArray?.forEach {
                    add(commonDiscriminator.inject(it))
                }
            }

            val newDataObject = commonDiscriminator.inject(dataObject)
            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                newDataObject,
                newIncludedArray
            )
            return commonDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val newDataObject = commonDiscriminator.extract(dataObject)
            val includedObject = getIncludedArray(jsonElement)
            val newIncludedArray = buildJsonArray {
                includedObject?.jsonArray?.forEach {
                    add(commonDiscriminator.extract(it))
                }
            }
            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                newDataObject,
                newIncludedArray
            )
            return commonDiscriminator.extract(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getDataEntry(dataObject: JsonElement): Map.Entry<String, JsonElement> {
        return object : Map.Entry<String, JsonElement> {
            override val key: String = DATA_KEY
            override val value: JsonElement = dataObject
        }
    }

    private fun getIncludedEntry(includedArray: JsonArray): Map.Entry<String, JsonArray> {
        return object : Map.Entry<String, JsonArray> {
            override val key: String = INCLUDED_KEY
            override val value: JsonArray = includedArray
        }
    }

    private fun getDataObject(jsonElement: JsonElement) = jsonElement.jsonObject[DATA_KEY]!!

    private fun getIncludedArray(jsonElement: JsonElement) = jsonElement.jsonObject[INCLUDED_KEY]

    private fun getJsonObjectWithDataDiscriminator(
        jsonElement: JsonElement,
        dataObject: JsonElement,
        includedArray: JsonArray
    ): JsonObject {
        val entries = jsonElement.jsonObject.entries.toMutableSet()
        entries.removeAll { it.key == DATA_KEY }
        entries.removeAll { it.key == INCLUDED_KEY }
        entries.add(getDataEntry(dataObject))
        entries.add(getIncludedEntry(includedArray))
        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        return JsonObject(resultMap)
    }
}
