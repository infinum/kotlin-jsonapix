package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultLinks
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable

@Serializable
public data class PersonModel(
  public val `data`: Person,
  public val rootLinks: DefaultLinks? = null,
  public val resourceObjectLinks: DefaultLinks? = null,
  public val relationshipsLinks: Map<String, DefaultLinks?>? = emptyMap(),
  public val errors: List<PersonalError>? = null,
  public val rootMeta: PersonRootMeta? = null,
  public val resourceObjectMeta: PersonResourceMeta? = null,
  public val relationshipsMeta: Map<String, PersonRelationshipMeta?>? = emptyMap(),
)
