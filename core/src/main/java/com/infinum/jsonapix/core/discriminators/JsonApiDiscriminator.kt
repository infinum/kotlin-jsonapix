package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * This Discriminator is made specifically to handle JSON API objects. It leverages the functionality
 * of [CommonDiscriminator] and handles the whole hierarchy of JSON API object.
 */
class JsonApiDiscriminator(discriminator: String): Discriminator {

    companion object {
        private const val DATA_KEY = "data"
    }

    private val commonDiscriminator = CommonDiscriminator(discriminator)

    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = jsonElement.jsonObject[DATA_KEY]!!
            val newDataObject = commonDiscriminator.inject(dataObject)
            val entries = jsonElement.jsonObject.entries.toMutableSet()
            entries.removeAll { it.key == DATA_KEY }
            entries.add(getDataEntry(newDataObject))
            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            val newJsonElement = JsonObject(resultMap)
            return commonDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = jsonElement.jsonObject[DATA_KEY]!!
            val newDataObject = commonDiscriminator.extract(dataObject)
            val entries = jsonElement.jsonObject.entries.toMutableSet()
            entries.removeAll { it.key == DATA_KEY }
            entries.add(getDataEntry(newDataObject))
            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            val newJsonElement = JsonObject(resultMap)
            return commonDiscriminator.extract(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getDataEntry(dataObject: JsonElement): Map.Entry<String, JsonElement> {
        return object: Map.Entry<String, JsonElement> {
            override val key: String = DATA_KEY
            override val value: JsonElement = dataObject
        }
    }
}