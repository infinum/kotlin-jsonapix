package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.requireNotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "ResourceObject_dog")
public class ResourceObject_Dog(
  @SerialName(value = "type")
  override val type: String = "dog",
  @SerialName(value = "id")
  override val id: String = "0",
  @SerialName(value = "attributes")
  override val attributes: Attributes_Dog? = null,
  @Transient
  override val relationships: Relationships? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: Meta? = null,
) : ResourceObject<Dog> {
  override fun original(included: List<ResourceObject<Any>>?): Dog {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Dog(
      age = requireNotNull(attributes?.age, "age"),
      name = requireNotNull(attributes?.name, "name"),
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
