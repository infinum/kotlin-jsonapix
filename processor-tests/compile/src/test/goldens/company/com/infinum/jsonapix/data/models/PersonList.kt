package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultLinks
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class PersonList(
  public val `data`: List<PersonItem>,
  public val rootLinks: DefaultLinks? = null,
  public val errors: List<PersonalError>? = null,
  public val rootMeta: PersonRootMeta? = null,
)
