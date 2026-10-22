package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "article")
public class JsonApiXList_Article(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<ArticleError>? = null,
  @SerialName(value = "links")
  override val links: ArticleRootLinks? = null,
  @SerialName(value = "meta")
  override val meta: ArticleRootMeta? = null,
  @SerialName(value = "data")
  override val `data`: List<ResourceObject_Article>,
) : JsonApiXList<Article, ArticleList> {
  @Transient
  override val original: ArticleList
    get() {
      val items = data.map {
      val original = it.original(included)
      ArticleItem(
          original,
          it.links,
          it.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? ArticleRelationshipsLinks },
          it.meta,
          it.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? ArticleRelationshipsMeta } )
      }
      return ArticleList(items, links, errors, meta)
    }
}
