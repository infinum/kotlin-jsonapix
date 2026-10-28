package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiX
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
public class JsonApiX_Article(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<ArticleError>? = null,
  @SerialName(value = "links")
  override val links: ArticleRootLinks? = null,
  @SerialName(value = "meta")
  override val meta: ArticleRootMeta? = null,
  @SerialName(value = "data")
  override val `data`: ResourceObject_Article,
) : JsonApiX<Article, ArticleModel> {
  @Transient
  override val original: ArticleModel
    get() {
      val original = data?.original(included)!!
      val model = ArticleModel(
          original,
          links,
          data?.links,
          data?.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? ArticleRelationshipsLinks },
          errors,
          meta,
          data?.meta,
          data?.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? ArticleRelationshipsMeta } )
      return model
    }
}
