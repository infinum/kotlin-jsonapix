package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "owner")
public class JsonApiXList_Owner(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<DefaultError>? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: Meta? = null,
  @SerialName(value = "data")
  override val `data`: List<ResourceObject_Owner>,
) : JsonApiXList<Owner, OwnerList> {
  @Transient
  override val original: OwnerList
    get() {
      val items = data.map {
      val original = it.original(included)
      OwnerItem(
          original,
          it.links,
          it.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? DefaultLinks },
          it.meta,
          it.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? Meta } )
      }
      return OwnerList(items, links, errors, meta)
    }
}
