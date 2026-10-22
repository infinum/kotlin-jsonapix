@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Article
import com.infinum.jsonapix.`data`.models.ArticleItem
import com.infinum.jsonapix.`data`.models.ArticleList
import com.infinum.jsonapix.`data`.models.ArticleModel
import com.infinum.jsonapix.`data`.models.ArticleResourceLinks
import com.infinum.jsonapix.`data`.models.ArticleResourceMeta
import com.infinum.jsonapix.`data`.models.Attributes_Article
import com.infinum.jsonapix.`data`.models.JsonApiXList_Article
import com.infinum.jsonapix.`data`.models.JsonApiX_Article
import com.infinum.jsonapix.`data`.models.Relationships_Article
import com.infinum.jsonapix.`data`.models.ResourceObject_Article
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
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

public fun Article.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Article = ResourceObject_Article(id = id() ?: "0", attributes = Attributes_Article.fromOriginalObject(this), relationships = Relationships_Article.fromOriginalObject(this), meta = meta as? ArticleResourceMeta, links = links as? ArticleResourceLinks)

public fun ArticleModel.toResourceObject(): ResourceObject_Article = ResourceObject_Article(id = data?.id() ?: "0", attributes = Attributes_Article.fromOriginalObject(data), relationships = Relationships_Article.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun ArticleItem.toResourceObject(): ResourceObject_Article = ResourceObject_Article(id = data?.id() ?: "0", attributes = Attributes_Article.fromOriginalObject(data), relationships = Relationships_Article.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun ArticleModel.toJsonApiX(): JsonApiX_Article = JsonApiX_Article(data = toResourceObject(), included = listOfNotNull(data.pinnedComment?.let{it.toResourceObject(
                    relationshipsMeta?.get("pinnedComment"),
                    relationshipsLinks?.get("pinnedComment")
                )}, *data.tags.mapSafe { it.let{it.toResourceObject(
                        relationshipsMeta?.get("tags"),
                        relationshipsLinks?.get("tags")
                    )}}.toTypedArray()), meta = rootMeta, links = rootLinks)

public fun ArticleList.toJsonApiXList(): JsonApiXList_Article = JsonApiXList_Article(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull(*data.mapSafe {item ->
                    item.data.pinnedComment?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("pinnedComment"),
                        item.relationshipsLinks?.get("pinnedComment")
                    )} }.toTypedArray(),*data.flatMapSafe {item-> item.data.tags.mapSafe {
it.toResourceObject(
                     item.relationshipsMeta?.get("tags"),
                     item.relationshipsLinks?.get("tags")
                )} }.toTypedArray()))

public fun ArticleModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringArticle")
public fun ArticleList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Article::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
