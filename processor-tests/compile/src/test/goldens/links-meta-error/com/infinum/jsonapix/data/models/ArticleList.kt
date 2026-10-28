package com.infinum.jsonapix.`data`.models

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class ArticleList(
  public val `data`: List<ArticleItem>,
  public val rootLinks: ArticleRootLinks? = null,
  public val errors: List<ArticleError>? = null,
  public val rootMeta: ArticleRootMeta? = null,
)
