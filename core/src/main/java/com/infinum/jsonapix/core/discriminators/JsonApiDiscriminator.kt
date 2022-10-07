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
    private val rootType: String,
    private val rootLinks: String,
    private val resourceObjectLinks: String,
    relationshipsLinks: String,
    meta: String
) : BaseJsonApiDiscriminator(rootType, relationshipsLinks, meta) {

    @SuppressWarnings("SwallowedException", "LongMethod")
    override fun inject(jsonElement: JsonElement): JsonElement {
        try {
            // Current objects
            val dataObject = getDataObject(jsonElement)
            val relationshipsObject = getRelationshipsObject(jsonElement)
            val attributesObject = getAttributesObject(jsonElement)
            val rootLinksObject = getLinksObject(jsonElement)
            val errorsObject = getErrorsObject(jsonElement)
            val resourceLinksObject = dataObject?.let {
                getLinksObject(it)
            }
            val metaObject = getMetaObject(jsonElement)

            // Injected objects
            val newRootLinksObject = rootLinksObject?.let {
                val linksDiscriminator = CommonDiscriminator(rootLinks)
                linksDiscriminator.inject(it)
            }
            val newResourceLinksObject = resourceLinksObject?.let {
                val resourceLinksDiscriminator = CommonDiscriminator(resourceObjectLinks)
                resourceLinksDiscriminator.inject(it)
            }

            val errorsArray = errorsObject?.jsonArray?.let {
                buildJsonArray {
                    it.forEach { jsonElement ->
                        val errorDiscriminator = CommonDiscriminator(
                            JsonApiConstants.Prefix.ERROR.withName(
                                TypeExtractor.findType(it)
                            )
                        )
                        add(errorDiscriminator.inject(jsonElement))
                    }
                }
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

            val newIncludedArray = buildTypeDiscriminatedIncludedArray(jsonElement)

            val newDataObject = dataObject?.let {
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

            val newMetaObject = metaObject?.let { getNewMetaObject(it) }

            val newJsonElement = getJsonObjectWithDataDiscriminator(
                original = jsonElement,
                dataObject = newDataObject,
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

    @SuppressWarnings("SwallowedException")
    override fun extract(jsonElement: JsonElement): JsonElement {
        try {
            val dataObject = getDataObject(jsonElement)?.let {
                rootDiscriminator.extract(it)
            }
            val includedArray = buildRootDiscriminatedIncludedArray(jsonElement)
            val errorsArray = buildRootDiscriminatedErrorsArray(jsonElement)
            val newJsonElement = getJsonObjectWithDataDiscriminator(
                original = jsonElement,
                includedArray = includedArray,
                dataObject = dataObject,
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
        getDataObject(jsonElement)?.jsonObject?.get(JsonApiConstants.Keys.RELATIONSHIPS)

    override fun getAttributesObject(jsonElement: JsonElement): JsonElement? =
        getDataObject(jsonElement)?.jsonObject?.get(JsonApiConstants.Keys.ATTRIBUTES)

    private fun getJsonObjectWithDataDiscriminator(
        original: JsonElement,
        dataObject: JsonElement?,
        includedArray: JsonArray?,
        linksObject: JsonElement?,
        errorsArray: JsonArray?,
        metaObject: JsonElement?
    ): JsonObject {
        return getDiscriminatedBaseEntries(original, includedArray, linksObject, errorsArray, metaObject).let { entries ->
            dataObject?.let { data ->
                entries.removeAll { it.key == JsonApiConstants.Keys.DATA }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.DATA, data))
            }

            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }
}
