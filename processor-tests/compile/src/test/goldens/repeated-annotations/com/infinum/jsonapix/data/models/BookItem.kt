package com.infinum.jsonapix.`data`.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class BookItem(
  public val `data`: Book,
  public val resourceObjectLinks: BookLinks? = null,
  public val relationshipsLinks: Map<String, BookLinks?>? = emptyMap(),
  public val resourceObjectMeta: BookMeta? = null,
  public val relationshipsMeta: Map<String, BookMeta?>? = emptyMap(),
)
