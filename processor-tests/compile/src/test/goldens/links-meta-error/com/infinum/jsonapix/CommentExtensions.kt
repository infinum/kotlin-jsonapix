@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Comment
import com.infinum.jsonapix.`data`.models.Comment
import com.infinum.jsonapix.`data`.models.CommentItem
import com.infinum.jsonapix.`data`.models.CommentList
import com.infinum.jsonapix.`data`.models.CommentModel
import com.infinum.jsonapix.`data`.models.JsonApiXList_Comment
import com.infinum.jsonapix.`data`.models.JsonApiX_Comment
import com.infinum.jsonapix.`data`.models.ResourceObject_Comment
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

public fun Comment.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Comment = ResourceObject_Comment(id = id() ?: "0", attributes = Attributes_Comment.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun CommentModel.toResourceObject(): ResourceObject_Comment = ResourceObject_Comment(id = data?.id() ?: "0", attributes = Attributes_Comment.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun CommentItem.toResourceObject(): ResourceObject_Comment = ResourceObject_Comment(id = data?.id() ?: "0", attributes = Attributes_Comment.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun CommentModel.toJsonApiX(): JsonApiX_Comment = JsonApiX_Comment(data = toResourceObject(), included = listOfNotNull(), meta = rootMeta, links = rootLinks)

public fun CommentList.toJsonApiXList(): JsonApiXList_Comment = JsonApiXList_Comment(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull())

public fun CommentModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringComment")
public fun CommentList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Comment::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
