@file:SuppressWarnings("TooGenericExceptionCaught")

package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * This Discriminator is made specifically to handle JSON API objects. It leverages the functionality
 * of [CommonDiscriminator] and handles the whole hierarchy of JSON API object.
 */
class JsonApiDiscriminator(
    private val rootDiscriminator: String
) : Discriminator {

    companion object {
        private const val DATA_KEY = "data"
        private const val INCLUDED_KEY = "included"
        private const val RELATIONSHIPS_KEY = "relationships"
        private const val ATTRIBUTES_KEY = "attributes"
        private const val ATTRIBUTES_PREFIX = "AttributesModel_"
        private const val RELATIONSHIPS_PREFIX = "RelationshipsModel_"
        private const val RESOURCE_OBJECT_PREFIX = "ResourceObject_"
    }

    private val commonDiscriminator = CommonDiscriminator(rootDiscriminator)

    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val relationshipsObject = getRelationshipsObject(jsonElement)
            val attributesObject = getAttributesObject(jsonElement)

            val relationshipsDiscriminator = CommonDiscriminator("$RELATIONSHIPS_PREFIX$rootDiscriminator")
            val newRelationshipsObject = relationshipsDiscriminator.inject(relationshipsObject)

            val attributesDiscriminator = CommonDiscriminator("$ATTRIBUTES_PREFIX$rootDiscriminator")
            val newAttributesObject = attributesDiscriminator.inject(attributesObject)

            val newIncludedArray = buildJsonArray {
                includedObject?.jsonArray?.forEach {
                    val includedDiscriminator = CommonDiscriminator("$RESOURCE_OBJECT_PREFIX${TypeExtractor.findType(it)}")
                    add(includedDiscriminator.inject(it))
                }
            }

            val dataDiscriminator = CommonDiscriminator("$RESOURCE_OBJECT_PREFIX$rootDiscriminator")
            val newDataObject = dataDiscriminator.inject(dataObject).apply {
                getNewDataObject(this, newAttributesObject, newRelationshipsObject)
            }
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

    private fun getAttributesEntry(attributesObject: JsonElement): Map.Entry<String, JsonElement> {
        return object : Map.Entry<String, JsonElement> {
            override val key: String = ATTRIBUTES_KEY
            override val value: JsonElement = attributesObject
        }
    }

    private fun getRelationshipsEntry(relationshipsObject: JsonElement): Map.Entry<String, JsonElement> {
        return object : Map.Entry<String, JsonElement> {
            override val key: String = RELATIONSHIPS_KEY
            override val value: JsonElement = relationshipsObject
        }
    }

    private fun getDataObject(jsonElement: JsonElement) = jsonElement.jsonObject[DATA_KEY]!!

    private fun getRelationshipsObject(jsonElement: JsonElement) = getDataObject(jsonElement).jsonObject[RELATIONSHIPS_KEY]!!

    private fun getAttributesObject(jsonElement: JsonElement) = getDataObject(jsonElement).jsonObject[ATTRIBUTES_KEY]!!

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

    private fun getNewDataObject(
        originalDataObject: JsonElement,
        newAttributesObject: JsonElement,
        newRelationshipsObject: JsonElement
    ): JsonObject {
        val entries = originalDataObject.jsonObject.entries.toMutableSet()
        entries.removeAll { it.key == ATTRIBUTES_KEY }
        entries.removeAll { it.key == RELATIONSHIPS_KEY }
        entries.add(getAttributesEntry(newAttributesObject))
        entries.add(getRelationshipsEntry(newRelationshipsObject))
        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        return JsonObject(resultMap)
    }
}
