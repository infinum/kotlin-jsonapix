package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class EmptyDataItem(
  public val `data`: EmptyData? = null,
  public val resourceObjectLinks: DefaultLinks? = null,
  public val relationshipsLinks: Map<String, DefaultLinks?>? = emptyMap(),
  public val resourceObjectMeta: Meta? = null,
  public val relationshipsMeta: Map<String, Meta?>? = emptyMap(),
)
