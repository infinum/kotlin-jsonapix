package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiX
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
@SerialName(value = "tag")
public class JsonApiX_Tag(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<DefaultError>? = null,
  @SerialName(value = "links")
  override val links: TagLinks? = null,
  @SerialName(value = "meta")
  override val meta: Meta? = null,
  @SerialName(value = "data")
  override val `data`: ResourceObject_Tag,
) : JsonApiX<Tag, TagModel> {
  @Transient
  override val original: TagModel
    get() {
      val original = data?.original(included)!!
      val model = TagModel(
          original,
          links,
          data?.links,
          data?.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? DefaultLinks },
          errors,
          meta,
          data?.meta,
          data?.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? Meta } )
      return model
    }
}
