@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Tag
import com.infinum.jsonapix.`data`.models.JsonApiXList_Tag
import com.infinum.jsonapix.`data`.models.JsonApiX_Tag
import com.infinum.jsonapix.`data`.models.ResourceObject_Tag
import com.infinum.jsonapix.`data`.models.Tag
import com.infinum.jsonapix.`data`.models.TagItem
import com.infinum.jsonapix.`data`.models.TagList
import com.infinum.jsonapix.`data`.models.TagModel
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

public fun Tag.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Tag = ResourceObject_Tag(id = id() ?: "0", attributes = Attributes_Tag.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun TagModel.toResourceObject(): ResourceObject_Tag = ResourceObject_Tag(id = data?.id() ?: "0", attributes = Attributes_Tag.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun TagItem.toResourceObject(): ResourceObject_Tag = ResourceObject_Tag(id = data?.id() ?: "0", attributes = Attributes_Tag.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun TagModel.toJsonApiX(): JsonApiX_Tag = JsonApiX_Tag(data = toResourceObject(), included = listOfNotNull(), meta = rootMeta, links = rootLinks)

public fun TagList.toJsonApiXList(): JsonApiXList_Tag = JsonApiXList_Tag(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull())

public fun TagModel.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiX()
  val type = jsonX.data.type
  val discriminator = JsonApiDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiX::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}

@JvmName("toJsonApiXStringTag")
public fun TagList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Tag::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
