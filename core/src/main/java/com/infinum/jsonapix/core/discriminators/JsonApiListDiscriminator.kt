package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class JsonApiListDiscriminator(
    private val rootType: String,
    private val rootLinks: String,
    private val resourceObjectLinks: String,
    private val relationshipsLinks: String
) : Discriminator {

    private val rootDiscriminator = CommonDiscriminator(rootType)

    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray = getDataArray(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val newDataEntries = mutableListOf<JsonElement>()
            val rootLinksObject = getLinksObject(jsonElement)

            val newRootLinksObject = rootLinksObject?.let {
                val linksDiscriminator = CommonDiscriminator(rootLinks)
                linksDiscriminator.inject(it)
            }

            dataArray?.jsonArray?.forEach { dataObject ->
                val relationshipsObject = getRelationshipsObject(dataObject)
                val attributesObject = getAttributesObject(dataObject)
                val resourceLinksObject = getLinksObject(dataObject)

                val relationshipsLinksObject = relationshipsObject?.let {
                    getLinksObject(it)
                }
                val newRelationshipsLinksObject = relationshipsLinksObject?.let {
                    val relationshipsLinksDiscriminator = CommonDiscriminator(relationshipsLinks)
                    relationshipsLinksDiscriminator.inject(it)
                }

                val newRelationshipsObject = relationshipsObject?.let {
                    val relationshipsDiscriminator = CommonDiscriminator(
                        JsonApiConstants.Prefix.RELATIONSHIPS.withName(rootType)
                    )
                    relationshipsDiscriminator.inject(it).apply {
                        getNewRelationshipsObject(this, newRelationshipsLinksObject)
                    }
                }

                val newAttributesObject = attributesObject?.let {
                    val attributesDiscriminator =
                        CommonDiscriminator(JsonApiConstants.Prefix.ATTRIBUTES.withName(rootType))
                    attributesDiscriminator.inject(it)
                }

                val newResourceLinksObject = resourceLinksObject?.let {
                    val resourceLinksDiscriminator = CommonDiscriminator(resourceObjectLinks)
                    resourceLinksDiscriminator.inject(it)
                }

                val newDataObject = dataObject.let {
                    val dataDiscriminator = CommonDiscriminator(
                        JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(rootType)
                    )
                    dataDiscriminator.inject(it).apply {
                        getNewDataObject(this, newAttributesObject, newRelationshipsObject, newResourceLinksObject)
                    }
                }
                newDataEntries.add(newDataObject)
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

            val newDataArray = buildJsonArray {
                newDataEntries.forEach {
                    add(it)
                }
            }

            val newJsonElement = getJsonObjectWithDataDiscriminator(
                jsonElement,
                newDataArray,
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

    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray = getDataArray(jsonElement)?.let {
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
                dataArray?.jsonArray,
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

    private fun getDataArray(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.DATA]

    private fun getRelationshipsObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.RELATIONSHIPS]

    private fun getAttributesObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.ATTRIBUTES]

    private fun getIncludedArray(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.INCLUDED]

    private fun getLinksObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.LINKS]

    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataArray: JsonArray?,
        includedArray: JsonArray?,
        linksObject: JsonElement?
    ): JsonObject {
        return original.jsonObject.entries.toMutableSet().let { entries ->
            dataArray?.let { data ->
                entries.removeAll { it.key == JsonApiConstants.Keys.DATA }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.DATA, data))
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
        original: JsonElement,
        linksObject: JsonElement?
    ) {
        return original.jsonObject.entries.toMutableSet().let { entries ->
            linksObject?.let {
                entries.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, linksObject))
            }
            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }
}
