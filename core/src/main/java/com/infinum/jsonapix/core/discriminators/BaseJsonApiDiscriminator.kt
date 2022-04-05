package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

abstract class BaseJsonApiDiscriminator(
    private val relationshipsLinks: String,
    private val meta: String
) : Discriminator {

    abstract override fun inject(jsonElement: JsonElement): JsonElement

    abstract override fun extract(jsonElement: JsonElement): JsonElement

    abstract fun getRelationshipsObject(jsonElement: JsonElement): JsonElement?

    abstract fun getAttributesObject(jsonElement: JsonElement): JsonElement?

    fun getJsonArrayEntry(key: String, data: JsonArray): Map.Entry<String, JsonArray> {
        return object : Map.Entry<String, JsonArray> {
            override val key: String = key
            override val value: JsonArray = data
        }
    }

    fun getNewMetaObject(original: JsonElement): JsonElement {
        val metaDiscriminator = CommonDiscriminator(meta)
        return metaDiscriminator.inject(original)
    }

    fun getDataObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.DATA]

    fun getIncludedArray(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.INCLUDED]

    fun getLinksObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.LINKS]

    fun getMetaObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.META]

    fun getJsonObjectEntry(key: String, data: JsonElement): Map.Entry<String, JsonElement> {
        return object : Map.Entry<String, JsonElement> {
            override val key: String = key
            override val value: JsonElement = data
        }
    }

    fun getNewDataObject(
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

    fun getNewRelationshipsObject(
        original: JsonElement
    ): JsonObject {
        val resultMap = mutableMapOf<String, JsonElement>()
        val relationshipsLinksDiscriminator = CommonDiscriminator(relationshipsLinks)
        original.jsonObject.entries.filter { it.value is JsonObject }.forEach { relationshipEntry ->
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
