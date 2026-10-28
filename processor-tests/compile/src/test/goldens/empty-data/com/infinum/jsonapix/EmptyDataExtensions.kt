@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.EmptyData
import com.infinum.jsonapix.`data`.models.EmptyDataItem
import com.infinum.jsonapix.`data`.models.EmptyDataList
import com.infinum.jsonapix.`data`.models.EmptyDataModel
import com.infinum.jsonapix.`data`.models.JsonApiXList_EmptyData
import com.infinum.jsonapix.`data`.models.JsonApiX_EmptyData
import com.infinum.jsonapix.`data`.models.ResourceObject_EmptyData
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.flatMapSafe
import com.infinum.jsonapix.core.shared.mapSafe
import kotlin.String
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

public fun EmptyData.toResourceObject(meta: Meta?, links: Links?): ResourceObject_EmptyData = ResourceObject_EmptyData(id = id() ?: "0", meta = meta as? Meta, links = links as? DefaultLinks)

public fun EmptyDataModel.toResourceObject(): ResourceObject_EmptyData = ResourceObject_EmptyData(id = data?.id() ?: "0", meta = resourceObjectMeta, links = resourceObjectLinks)

public fun EmptyDataItem.toResourceObject(): ResourceObject_EmptyData = ResourceObject_EmptyData(id = data?.id() ?: "0", meta = resourceObjectMeta, links = resourceObjectLinks)

public fun EmptyDataModel.toJsonApiX(): JsonApiX_EmptyData = JsonApiX_EmptyData(data = toResourceObject(), included = listOfNotNull(), meta = rootMeta, links = rootLinks)

public fun EmptyDataList.toJsonApiXList(): JsonApiXList_EmptyData = JsonApiXList_EmptyData(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull())

public fun EmptyDataModel.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiX()
  val type = jsonX.data?.type ?: TypeExtractor.guessType(this::class)
  val discriminator = JsonApiDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiX::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}

@JvmName("toJsonApiXStringEmptyData")
public fun EmptyDataList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(EmptyData::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
