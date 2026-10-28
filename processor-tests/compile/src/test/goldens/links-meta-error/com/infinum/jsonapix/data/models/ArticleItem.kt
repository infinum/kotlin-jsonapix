package com.infinum.jsonapix.`data`.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class ArticleItem(
  public val `data`: Article,
  public val resourceObjectLinks: ArticleResourceLinks? = null,
  public val relationshipsLinks: Map<String, ArticleRelationshipsLinks?>? = emptyMap(),
  public val resourceObjectMeta: ArticleResourceMeta? = null,
  public val relationshipsMeta: Map<String, ArticleRelationshipsMeta?>? = emptyMap(),
)
