package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class EmptyDataList(
  public val `data`: List<EmptyDataItem>? = null,
  public val rootLinks: DefaultLinks? = null,
  public val errors: List<DefaultError>? = null,
  public val rootMeta: EmptyDataMeta? = null,
)
