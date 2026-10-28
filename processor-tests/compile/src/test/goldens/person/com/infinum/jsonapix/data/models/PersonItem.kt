package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultLinks
import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class PersonItem(
  public val `data`: Person,
  public val resourceObjectLinks: DefaultLinks? = null,
  public val relationshipsLinks: Map<String, DefaultLinks?>? = emptyMap(),
  public val resourceObjectMeta: PersonResourceMeta? = null,
  public val relationshipsMeta: Map<String, PersonRelationshipMeta?>? = emptyMap(),
)
