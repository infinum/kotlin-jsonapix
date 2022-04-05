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
    private val relationshipsLinks: String,
    private val meta: String
) : BaseJsonApiDiscriminator(relationshipsLinks, meta) {

    private val rootDiscriminator = CommonDiscriminator(rootType)

    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught", "LongMethod")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray = getDataObject(jsonElement)
            val includedObject = getIncludedArray(jsonElement)
            val newDataEntries = mutableListOf<JsonElement>()
            val rootLinksObject = getLinksObject(jsonElement)
            val metaObject = getMetaObject(jsonElement)

            val newRootLinksObject = rootLinksObject?.let {
                val linksDiscriminator = CommonDiscriminator(rootLinks)
                linksDiscriminator.inject(it)
            }

            dataArray?.jsonArray?.forEach { dataObject ->
                val relationshipsObject = getRelationshipsObject(dataObject)
                val attributesObject = getAttributesObject(dataObject)
                val resourceLinksObject = getLinksObject(dataObject)

                val newRelationshipsObject = relationshipsObject?.let {
                    val relationshipsDiscriminator = CommonDiscriminator(
                        JsonApiConstants.Prefix.RELATIONSHIPS.withName(rootType)
                    )
                    getNewRelationshipsObject(relationshipsDiscriminator.inject(it))
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
                    getNewDataObject(
                        dataDiscriminator.inject(it),
                        newAttributesObject,
                        newRelationshipsObject,
                        newResourceLinksObject
                    )
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

            val newMetaObject = metaObject?.let { getNewMetaObject(it) }

            val newJsonElement = getJsonObjectWithDataDiscriminator(
                original = jsonElement,
                dataArray = newDataArray,
                includedArray = newIncludedArray,
                linksObject = newRootLinksObject,
                metaObject = newMetaObject
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
            val dataArray = getDataObject(jsonElement)?.let {
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
                original = jsonElement,
                dataArray = dataArray?.jsonArray,
                includedArray = includedArray,
                linksObject = null,
                metaObject = null
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

    override fun getRelationshipsObject(jsonElement: JsonElement): JsonElement? =
        jsonElement.jsonObject[JsonApiConstants.Keys.RELATIONSHIPS]

    override fun getAttributesObject(jsonElement: JsonElement): JsonElement? =
        jsonElement.jsonObject[JsonApiConstants.Keys.ATTRIBUTES]

    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataArray: JsonArray?,
        includedArray: JsonArray?,
        linksObject: JsonElement?,
        metaObject: JsonElement?
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

            metaObject?.let { meta ->
                entries.removeAll { it.key == JsonApiConstants.Keys.META }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.META, meta))
            }

            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }
}
