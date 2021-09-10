@file:SuppressWarnings("TooGenericExceptionCaught")

package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * This Discriminator is made specifically to handle JSON API objects. It leverages the functionality
 * of [CommonDiscriminator] and handles the whole hierarchy of a JSON API object.
 * All child objects or arrays that extend an interface get their discriminator string injected or extracted here
 * Discriminator string should always be in the following format:
 *
 * Root object -> type parameter of the class
 * Child objects -> Child prefix + type parameter e.g. Attributes_person where person is the type
 * of a class called Person passed as a parameter to JsonApiX annotation.
 */
class JsonApiDiscriminator(
    private val rootType: String
) : Discriminator {

    private val rootDiscriminator = CommonDiscriminator(rootType)

    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val relationshipsObject = getRelationshipsObject(jsonElement)
            val attributesObject = getAttributesObject(jsonElement)

            val newRelationshipsObject = relationshipsObject?.let {
                val relationshipsDiscriminator = CommonDiscriminator(
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(rootType)
                )
                relationshipsDiscriminator.inject(it)
            }

            val newAttributesObject = attributesObject?.let {
                val attributesDiscriminator =
                    CommonDiscriminator(JsonApiConstants.Prefix.ATTRIBUTES.withName(rootType))
                attributesDiscriminator.inject(it)
            }

            val newIncludedArray = includedObject?.let {
                buildJsonArray {
                    it.jsonArray.forEach {
                        val includedDiscriminator =
                            CommonDiscriminator(
                                JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(
                                    TypeExtractor.findType(it)
                                )
                            )
                        add(includedDiscriminator.inject(it))
                    }
                }
            }

            val newDataObject = dataObject?.let {
                val dataDiscriminator = CommonDiscriminator(
                    JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(rootType)
                )
                dataDiscriminator.inject(it).apply {
                    getNewDataObject(this, newAttributesObject, newRelationshipsObject)
                }
            }

            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                newDataObject,
                newIncludedArray
            )
            return rootDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)?.let {
                rootDiscriminator.extract(it)
            }
            val includedArray = getIncludedArray(jsonElement)?.let { included ->
                included.jsonArray.let {
                    buildJsonArray {
                        it.forEach {
                            add(rootDiscriminator.extract(it))
                        }
                    }
                }
            }
            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                dataObject,
                includedArray
            )
            return rootDiscriminator.extract(newJsonElement)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getJsonObjectEntry(key: String, data: JsonElement): Map.Entry<String, JsonElement> {
        return object : Map.Entry<String, JsonElement> {
            override val key: String = key
            override val value: JsonElement = data
        }
    }

    private fun getJsonArrayEntry(key: String, data: JsonArray): Map.Entry<String, JsonArray> {
        return object : Map.Entry<String, JsonArray> {
            override val key: String = key
            override val value: JsonArray = data
        }
    }

    private fun getDataObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.DATA]

    private fun getRelationshipsObject(jsonElement: JsonElement) =
        getDataObject(jsonElement)?.jsonObject?.get(JsonApiConstants.Keys.RELATIONSHIPS)

    private fun getAttributesObject(jsonElement: JsonElement) =
        getDataObject(jsonElement)?.jsonObject?.get(JsonApiConstants.Keys.ATTRIBUTES)

    private fun getIncludedArray(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.INCLUDED]

    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataObject: JsonElement?,
        includedArray: JsonArray?
    ): JsonObject {
        val entries = original.jsonObject.entries.toMutableSet().apply {
            dataObject?.let { data ->
                removeAll { it.key == JsonApiConstants.Keys.DATA }
                add(getJsonObjectEntry(JsonApiConstants.Keys.DATA, data))
            }
            includedArray?.let { included ->
                removeAll { it.key == JsonApiConstants.Keys.INCLUDED }
                add(getJsonArrayEntry(JsonApiConstants.Keys.INCLUDED, included))
            }
        }

        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })

        return JsonObject(resultMap)
    }

    private fun getNewDataObject(
        original: JsonElement,
        attributesObject: JsonElement?,
        relationshipsObject: JsonElement?
    ): JsonObject {
        val entries = original.jsonObject.entries.toMutableSet().apply {
            attributesObject?.let { attributes ->
                removeAll { it.key == JsonApiConstants.Keys.ATTRIBUTES }
                add(getJsonObjectEntry(JsonApiConstants.Keys.ATTRIBUTES, attributes))
            }

            relationshipsObject?.let { relationships ->
                removeAll { it.key == JsonApiConstants.Keys.RELATIONSHIPS }
                add(getJsonObjectEntry(JsonApiConstants.Keys.RELATIONSHIPS, relationships))
            }
        }

        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        return JsonObject(resultMap)
    }
}
