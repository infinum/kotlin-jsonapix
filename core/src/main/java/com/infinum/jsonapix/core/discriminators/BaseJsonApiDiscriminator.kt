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

@Suppress("TooManyFunctions")
abstract class BaseJsonApiDiscriminator(
    rootType: String,
    private val relationshipsLinks: String,
    private val relationshipsMeta: String,
    private val error: String,
) : Discriminator {

    val rootDiscriminator = CommonDiscriminator(rootType)

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

    fun getDataObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.DATA].takeUnless { it is JsonNull }

    fun getIncludedArray(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.INCLUDED].takeUnless { it is JsonNull }

    fun getLinksObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.LINKS].takeUnless { it is JsonNull }

    fun getMetaObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.META].takeUnless { it is JsonNull }

    fun getErrorsObject(jsonElement: JsonElement) =
        jsonElement.jsonObject[JsonApiConstants.Keys.ERRORS].takeUnless { it is JsonNull }

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
        linksObject: JsonElement?,
        metaObject: JsonElement?,
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

            metaObject?.let { meta ->
                entries.removeAll { it.key == JsonApiConstants.Keys.META }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.META, meta))
            }

            val resultMap = mutableMapOf<String, JsonElement>()
            resultMap.putAll(entries.map { Pair(it.key, it.value) })
            JsonObject(resultMap)
        }
    }

    /**
     * Processes and validates relationship objects from JSON API response.
     * 
     * This method validates that relationship objects do not have an explicit
     * null value for the 'data' field. According to JSON:API spec, if a relationship
     * object is present, its data field should either contain valid data or be omitted.
     * An explicit null value is considered invalid.
     */
    fun getNewRelationshipsObject(
        original: JsonElement,
    ): JsonObject {
        val resultMap = mutableMapOf<String, JsonElement>()
        val relationshipsLinksDiscriminator = CommonDiscriminator(relationshipsLinks)
        val relationshipsMetaDiscriminator = CommonDiscriminator(relationshipsMeta)
        original.jsonObject.entries.filter { it.value is JsonObject }.forEach { relationshipEntry ->
            val relationshipObject = relationshipEntry.value.jsonObject
            val dataField = relationshipObject[JsonApiConstants.Keys.DATA]
            
            // Validate that if data field is present, it is not explicitly null
            if (dataField is JsonNull) {
                throw IllegalArgumentException(
                    "Invalid relationship data: relationship '${relationshipEntry.key}' " +
                    "has explicit null data field. Relationships should either omit the " +
                    "data field or provide valid relationship data."
                )
            }
            
            val set = relationshipObject.entries.toMutableSet()
            getLinksObject(relationshipEntry.value)?.let { linksObject ->
                if (linksObject !is JsonNull) {
                    val newLinks = relationshipsLinksDiscriminator.inject(linksObject)
                    set.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                    set.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, newLinks))
                }
            }

            getMetaObject(relationshipEntry.value)?.let { metaObject ->
                if (metaObject !is JsonNull) {
                    val newLinks = relationshipsMetaDiscriminator.inject(metaObject)
                    set.removeAll { it.key == JsonApiConstants.Keys.META }
                    set.add(getJsonObjectEntry(JsonApiConstants.Keys.META, newLinks))
                }
            }
            val tempMap = mutableMapOf<String, JsonElement>()
            tempMap.putAll(set.map { Pair(it.key, it.value) })
            resultMap[relationshipEntry.key] = JsonObject(tempMap)
        }
        return JsonObject(resultMap)
    }

    fun getNewErrorsArray(
        original: JsonElement,
    ): JsonArray {
        val errorDiscriminator = CommonDiscriminator(error)

        return buildJsonArray {
            original.jsonArray.forEach { errorEntry ->
                add(errorDiscriminator.inject(errorEntry))
            }
        }
    }

    fun buildRootDiscriminatedIncludedArray(jsonElement: JsonElement) =
        getIncludedArray(jsonElement)?.takeIf { it !is JsonNull }?.let { included ->
            buildJsonArray {
                included.jsonArray.forEach {
                    add(rootDiscriminator.extract(it))
                }
            }
        }

    fun buildRootDiscriminatedErrorsArray(jsonElement: JsonElement) =
        getErrorsObject(jsonElement)?.takeIf { it !is JsonNull }?.let { errors ->
            buildJsonArray {
                errors.jsonArray.forEach {
                    add(rootDiscriminator.extract(it))
                }
            }
        }

    fun buildTypeDiscriminatedIncludedArray(jsonElement: JsonElement) =
        getIncludedArray(jsonElement)?.takeIf { it !is JsonNull }?.let { included ->
            buildJsonArray {
                included.jsonArray.forEach {
                    val includedDiscriminator =
                        CommonDiscriminator(
                            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(
                                requireNotNull(TypeExtractor.findType(it)),
                            ),
                        )
                    add(includedDiscriminator.inject(it))
                }
            }
        }

    fun getDiscriminatedBaseEntries(
        original: JsonElement,
        includedArray: JsonArray?,
        linksObject: JsonElement?,
        errorsArray: JsonArray?,
        metaObject: JsonElement?,
    ): MutableSet<Map.Entry<String, JsonElement>> {
        original.jsonObject.entries.toMutableSet().let { entries ->
            includedArray?.let { included ->
                entries.removeAll { it.key == JsonApiConstants.Keys.INCLUDED }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.INCLUDED, included))
            }

            linksObject?.let { links ->
                entries.removeAll { it.key == JsonApiConstants.Keys.LINKS }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.LINKS, links))
            }

            errorsArray?.let { errors ->
                entries.removeAll { it.key == JsonApiConstants.Keys.ERRORS }
                entries.add(getJsonArrayEntry(JsonApiConstants.Keys.ERRORS, errors))
            }

            metaObject?.let { meta ->
                entries.removeAll { it.key == JsonApiConstants.Keys.META }
                entries.add(getJsonObjectEntry(JsonApiConstants.Keys.META, meta))
            }

            return entries
        }
    }
}
