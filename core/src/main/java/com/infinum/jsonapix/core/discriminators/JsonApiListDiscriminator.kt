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
    relationshipsLinks: String,
    meta: String
) : BaseJsonApiDiscriminator(rootType, relationshipsLinks, meta) {

    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught", "LongMethod")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray = getDataObject(jsonElement)
            val newDataEntries = mutableListOf<JsonElement>()
            val rootLinksObject = getLinksObject(jsonElement)
            val errorsObject = getErrorsObject(jsonElement)
            val metaObject = getMetaObject(jsonElement)

            val newRootLinksObject = rootLinksObject?.let {
                val linksDiscriminator = CommonDiscriminator(rootLinks)
                linksDiscriminator.inject(it)
            }

            val errorsArray = errorsObject?.jsonArray?.let {
                buildJsonArray {
                    it.forEach { jsonElement ->
                        val errorDiscriminator =
                            CommonDiscriminator(JsonApiConstants.Prefix.ERROR.withName(TypeExtractor.findType(jsonElement)))
                        add(errorDiscriminator.inject(jsonElement))
                    }
                }
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

            val newIncludedArray = buildTypeDiscriminatedIncludedArray(jsonElement)

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
                errorsArray = errorsArray,
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
            val includedArray = buildRootDiscriminatedIncludedArray(jsonElement)
            val errorsArray = buildRootDiscriminatedErrorsArray(jsonElement)
            val newJsonElement = getJsonObjectWithDataDiscriminator(
                original = jsonElement,
                includedArray = includedArray,
                dataArray = dataArray?.jsonArray,
                linksObject = null,
                errorsArray = errorsArray,
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
        errorsArray: JsonArray?,
        metaObject: JsonElement?
    ): JsonObject {
        return getDiscriminatedBaseEntries(original, includedArray, linksObject, errorsArray, metaObject).let { entries ->
            dataArray?.let { data ->
                entries.removeAll { it.key == JsonApiConstants.Keys.DATA }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.DATA, data))
            }

            val resultMap = mutableMapOf<String, JsonElement>().apply {
                putAll(entries.map { Pair(it.key, it.value) })
            }
            JsonObject(resultMap)
        }
    }
}
