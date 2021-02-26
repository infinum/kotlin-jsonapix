@file:SuppressWarnings("TooGenericExceptionCaught")

package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * This Discriminator is made specifically to handle JSON API objects. It leverages the functionality
 * of [CommonDiscriminator] and handles the whole hierarchy of JSON API object.
 */
class JsonApiDiscriminator(discriminator: String) : Discriminator {

    companion object {
        private const val DATA_KEY = "data"
    }

    private val commonDiscriminator = CommonDiscriminator(discriminator)

    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val newDataObject = commonDiscriminator.inject(dataObject)
            val newJsonElement = getJsonObjectWithDataDiscriminator(jsonElement, newDataObject)
            return commonDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val newDataObject = commonDiscriminator.extract(dataObject)
            val newJsonElement = getJsonObjectWithDataDiscriminator(jsonElement, newDataObject)
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

    private fun getDataObject(jsonElement: JsonElement) = jsonElement.jsonObject[DATA_KEY]!!

    private fun getJsonObjectWithDataDiscriminator(
        jsonElement: JsonElement,
        dataObject: JsonElement
    ): JsonObject {
        val entries = jsonElement.jsonObject.entries.toMutableSet()
        entries.removeAll { it.key == DATA_KEY }
        entries.add(getDataEntry(dataObject))
        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        return JsonObject(resultMap)
    }
}
