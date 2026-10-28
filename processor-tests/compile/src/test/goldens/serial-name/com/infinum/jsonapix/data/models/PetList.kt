package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class PetList(
  public val `data`: List<PetItem>,
  public val rootLinks: DefaultLinks? = null,
  public val errors: List<DefaultError>? = null,
  public val rootMeta: Meta? = null,
)
