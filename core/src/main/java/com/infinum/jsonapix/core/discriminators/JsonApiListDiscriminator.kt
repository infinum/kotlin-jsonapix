package com.infinum.jsonapix.core.discriminators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@Suppress("LongParameterList")
class JsonApiListDiscriminator(
    private val rootType: String,
    private val rootLinks: String,
    private val resourceObjectLinks: String,
    private val relationshipsLinks: String,
    private val rootMeta: String,
    private val resourceObjectMeta: String,
    private val relationshipsMeta: String,
    private val error: String,
) : BaseJsonApiDiscriminator(rootType, relationshipsLinks, relationshipsMeta, error) {
    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught", "LongMethod")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray = getDataObject(jsonElement)
            val newDataEntries = mutableListOf<JsonElement>()
            val rootLinksObject = getLinksObject(jsonElement)
            val errorsObject = getErrorsObject(jsonElement)
            val rootMetaObject = getMetaObject(jsonElement)

            val newRootLinksObject =
                rootLinksObject?.takeIf { it !is JsonNull }?.let {
                    val linksDiscriminator = CommonDiscriminator(rootLinks)
                    linksDiscriminator.inject(it)
                }

            val newErrorsArray =
                errorsObject?.takeIf { it !is JsonNull }?.let {
                    getNewErrorsArray(it)
                }

            dataArray?.jsonArray?.forEach { dataObject ->
                val relationshipsObject = getRelationshipsObject(dataObject)
                val attributesObject = getAttributesObject(dataObject)
                val resourceLinksObject = getLinksObject(dataObject)
                val resourceMetaObject = getMetaObject(dataObject)

                val newRelationshipsObject =
                    relationshipsObject?.takeIf { it !is JsonNull }?.let {
                        val relationshipsDiscriminator =
                            CommonDiscriminator(
                                JsonApiConstants.Prefix.RELATIONSHIPS.withName(rootType),
                            )
                        relationshipsDiscriminator.inject(getNewRelationshipsObject(it))
                    }

                val newAttributesObject =
                    attributesObject?.takeIf { it !is JsonNull }?.let {
                        val attributesDiscriminator =
                            CommonDiscriminator(JsonApiConstants.Prefix.ATTRIBUTES.withName(rootType))
                        attributesDiscriminator.inject(it)
                    }

                val newResourceLinksObject =
                    resourceLinksObject?.takeIf { it !is JsonNull }?.let {
                        val resourceLinksDiscriminator = CommonDiscriminator(resourceObjectLinks)
                        resourceLinksDiscriminator.inject(it)
                    }

                val newResourceMetaObject =
                    resourceMetaObject?.takeIf { it !is JsonNull }?.let {
                        val resourceMetaDiscriminator = CommonDiscriminator(resourceObjectMeta)
                        resourceMetaDiscriminator.inject(it)
                    }

                dataObject.takeIf { it !is JsonNull }?.let {
                    val dataDiscriminator =
                        CommonDiscriminator(
                            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(rootType),
                        )
                    val newDataObject =
                        getNewDataObject(
                            dataDiscriminator.inject(it),
                            newAttributesObject,
                            newRelationshipsObject,
                            newResourceLinksObject,
                            newResourceMetaObject,
                        )
                    newDataEntries.add(newDataObject)
                }
            }

            val newIncludedArray = buildTypeDiscriminatedIncludedArray(jsonElement)

            val newDataArray =
                buildJsonArray {
                    newDataEntries.forEach {
                        add(it)
                    }
                }

            val newMetaObject =
                rootMetaObject?.takeIf { it !is JsonNull }?.let {
                    val resourceMetaDiscriminator = CommonDiscriminator(rootMeta)
                    resourceMetaDiscriminator.inject(it)
                }

            val newJsonElement =
                getJsonObjectWithDataDiscriminator(
                    original = jsonElement,
                    dataArray = newDataArray,
                    includedArray = newIncludedArray,
                    linksObject = newRootLinksObject,
                    errorsArray = newErrorsArray,
                    metaObject = newMetaObject,
                )
            return rootDiscriminator.inject(newJsonElement)
        } catch (e: Exception) {
            // TODO Add Timber and custom exceptions
            throw IllegalArgumentException(
                "Input must be either JSON object or array with the key type defined",
                e.cause,
            )
        }
    }

    // TODO Handle those in a future PR
    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataArray =
                getDataObject(jsonElement)?.let {
                    rootDiscriminator.extract(it)
                }
            val includedArray = buildRootDiscriminatedIncludedArray(jsonElement)
            val errorsArray = buildRootDiscriminatedErrorsArray(jsonElement)
            val newJsonElement =
                getJsonObjectWithDataDiscriminator(
                    original = jsonElement,
                    includedArray = includedArray,
                    dataArray = dataArray?.jsonArray,
                    linksObject = null,
                    errorsArray = errorsArray,
                    metaObject = null,
                )
            return rootDiscriminator.extract(newJsonElement)
        } catch (e: Exception) {
            // TODO Add Timber and custom exceptions
            throw IllegalArgumentException(
                "Input must be either JSON object or array with the key type defined",
                e.cause,
            )
        }
    }

    override fun getRelationshipsObject(jsonElement: JsonElement): JsonElement? = jsonElement.jsonObject[JsonApiConstants.Keys.RELATIONSHIPS]

    override fun getAttributesObject(jsonElement: JsonElement): JsonElement? = jsonElement.jsonObject[JsonApiConstants.Keys.ATTRIBUTES]

    @Suppress("LongParameterList")
    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataArray: JsonArray?,
        includedArray: JsonArray?,
        linksObject: JsonElement?,
        errorsArray: JsonArray?,
        metaObject: JsonElement?,
    ): JsonObject =
        getDiscriminatedBaseEntries(
            original,
            includedArray,
            linksObject,
            errorsArray,
            metaObject,
        ).let { entries ->
            dataArray?.let { data ->
                entries.removeAll { it.key == JsonApiConstants.Keys.DATA }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.DATA, data))
            }

            val resultMap =
                mutableMapOf<String, JsonElement>().apply {
                    putAll(entries.map { Pair(it.key, it.value) })
                }
            JsonObject(resultMap)
        }
}
