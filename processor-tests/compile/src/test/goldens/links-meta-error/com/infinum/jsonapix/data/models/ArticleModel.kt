package com.infinum.jsonapix.`data`.models

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class ArticleModel(
  public val `data`: Article,
  public val rootLinks: ArticleRootLinks? = null,
  public val resourceObjectLinks: ArticleResourceLinks? = null,
  public val relationshipsLinks: Map<String, ArticleRelationshipsLinks?>? = emptyMap(),
  public val errors: List<ArticleError>? = null,
  public val rootMeta: ArticleRootMeta? = null,
  public val resourceObjectMeta: ArticleResourceMeta? = null,
  public val relationshipsMeta: Map<String, ArticleRelationshipsMeta?>? = emptyMap(),
)
