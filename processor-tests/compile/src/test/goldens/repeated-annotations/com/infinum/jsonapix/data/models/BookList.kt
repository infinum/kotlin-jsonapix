package com.infinum.jsonapix.`data`.models

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class BookList(
  public val `data`: List<BookItem>,
  public val rootLinks: BookLinks? = null,
  public val errors: List<LibraryError>? = null,
  public val rootMeta: BookMeta? = null,
)
