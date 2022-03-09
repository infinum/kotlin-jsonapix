@file:SuppressWarnings("TooGenericExceptionCaught")

package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import java.awt.geom.PathIterator
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
    private val rootType: String,
    private val rootLinks: String,
    private val resourceObjectLinks: String,
    private val relationshipsLinks: String
) : Discriminator {

    private val rootDiscriminator = CommonDiscriminator(rootType)

    @SuppressWarnings("SwallowedException")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            // Current objects
            val dataObject = getDataObject(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val relationshipsObject = getRelationshipsObject(jsonElement)
            val attributesObject = getAttributesObject(jsonElement)
            val rootLinksObject = getLinksObject(jsonElement)
            val resourceLinksObject = dataObject?.let {
                getLinksObject(it)
            }

            // Injected objects
            val newRootLinksObject = rootLinksObject?.let {
                val linksDiscriminator = CommonDiscriminator(rootLinks)
                linksDiscriminator.inject(it)
            }
            val newResourceLinksObject = resourceLinksObject?.let {
                val resourceLinksDiscriminator = CommonDiscriminator(resourceObjectLinks)
                resourceLinksDiscriminator.inject(it)
            }

            val newRelationshipsObject = relationshipsObject?.let {
                val relationshipsDiscriminator = CommonDiscriminator(
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(rootType)
                )
                relationshipsDiscriminator.inject(getNewRelationshipsObject(it))
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
                getNewDataObject(dataDiscriminator.inject(it), newAttributesObject, newRelationshipsObject, newResourceLinksObject)
            }

            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                newDataObject,
                newIncludedArray,
                newRootLinksObject
            )
            return rootDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            // TODO Add Timber and custom exceptions
            throw IllegalArgumentException(
                "Input must be either JSON object or array with the key type defined",
                e.cause
            )
        }
    }

    @SuppressWarnings("SwallowedException")
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
                includedArray,
                null
            )
            return rootDiscriminator.extract(newJsonElement)
        } catch (e: Exception) {
            // TODO Add Timber and custom exceptions
            throw IllegalArgumentException(
                "Input must be either JSON object or array with the key type defined",
                e.cause
            )
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

    private fun getLinksObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.LINKS]

    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataObject: JsonElement?,
        includedArray: JsonArray?,
        linksObject: JsonElement?
    ): JsonObject {
        return original.jsonObject.entries.toMutableSet().let { entries ->
            dataObject?.let { data ->
                entries.removeAll { it.key == JsonApiConstants.Keys.DATA }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.DATA, data))
            }

            includedArray?.let { included ->
                entries.removeAll { it.key == JsonApiConstants.Keys.INCLUDED }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.INCLUDED, included))
            }

            linksObject?.let { links ->
                entries.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, links))
            }

            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }

    private fun getNewDataObject(
        original: JsonElement,
        attributesObject: JsonElement?,
        relationshipsObject: JsonElement?,
        linksObject: JsonElement?
    ): JsonObject {
        return original.jsonObject.entries.toMutableSet().let { entries ->
            attributesObject?.let { attributes ->
                entries.removeAll { it.key == JsonApiConstants.Keys.ATTRIBUTES }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.ATTRIBUTES, attributes))
            }

            relationshipsObject?.let { relationships ->
                entries.removeAll { it.key == JsonApiConstants.Keys.RELATIONSHIPS }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.RELATIONSHIPS, relationships))
            }

            linksObject?.let { links ->
                entries.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, links))
            }

            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }

    private fun getNewRelationshipsObject(
        original: JsonElement
    ): JsonObject {
        val resultMap = mutableMapOf<String, JsonElement>()
        val relationshipsLinksDiscriminator = CommonDiscriminator(relationshipsLinks)
        original.jsonObject.entries.forEach { relationshipEntry ->
            val set = relationshipEntry.value.jsonObject.entries.toMutableSet()
            getLinksObject(relationshipEntry.value)?.let { linksSafe ->
                val newLinks = relationshipsLinksDiscriminator.inject(linksSafe)
                set.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                set.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, newLinks))
            }
            val tempMap = mutableMapOf<String, JsonElement>()
            tempMap.putAll(set.map { Pair(it.key, it.value) })
            resultMap[relationshipEntry.key] = JsonObject(tempMap)
        }
        return JsonObject(resultMap)
    }
}
