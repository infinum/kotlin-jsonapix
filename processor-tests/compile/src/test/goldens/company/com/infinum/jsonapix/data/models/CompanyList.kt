package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class CompanyList(
  public val `data`: List<CompanyItem>,
  public val rootLinks: DefaultLinks? = null,
  public val errors: List<DefaultError>? = null,
  public val rootMeta: Meta? = null,
)
