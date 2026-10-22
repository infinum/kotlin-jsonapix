package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class DogModel(
  public val `data`: Dog,
  public val rootLinks: DefaultLinks? = null,
  public val resourceObjectLinks: DefaultLinks? = null,
  public val relationshipsLinks: Map<String, DefaultLinks?>? = emptyMap(),
  public val errors: List<DefaultError>? = null,
  public val rootMeta: Meta? = null,
  public val resourceObjectMeta: Meta? = null,
  public val relationshipsMeta: Map<String, Meta?>? = emptyMap(),
)
