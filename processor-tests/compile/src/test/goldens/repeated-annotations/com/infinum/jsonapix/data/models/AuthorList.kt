package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class AuthorList(
  public val `data`: List<AuthorItem>,
  public val rootLinks: DefaultLinks? = null,
  public val errors: List<LibraryError>? = null,
  public val rootMeta: Meta? = null,
)
