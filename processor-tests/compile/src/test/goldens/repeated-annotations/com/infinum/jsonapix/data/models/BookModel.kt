package com.infinum.jsonapix.`data`.models

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class BookModel(
  public val `data`: Book,
  public val rootLinks: BookLinks? = null,
  public val resourceObjectLinks: BookLinks? = null,
  public val relationshipsLinks: Map<String, BookLinks?>? = emptyMap(),
  public val errors: List<LibraryError>? = null,
  public val rootMeta: BookMeta? = null,
  public val resourceObjectMeta: BookMeta? = null,
  public val relationshipsMeta: Map<String, BookMeta?>? = emptyMap(),
)
