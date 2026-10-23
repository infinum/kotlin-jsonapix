package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.requireNotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "ResourceObject_owner")
public class ResourceObject_Owner(
  @SerialName(value = "type")
  override val type: String = "owner",
  @SerialName(value = "id")
  override val id: String = "0",
  @SerialName(value = "attributes")
  override val attributes: Attributes_Owner? = null,
  @SerialName(value = "relationships")
  override val relationships: Relationships_Owner? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: Meta? = null,
) : ResourceObject<Owner> {
  override fun original(included: List<ResourceObject<Any>>?): Owner {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Owner(
      firstName = requireNotNull(attributes?.firstName, "firstName"),
      lastName = attributes?.lastName,
      age = requireNotNull(attributes?.age, "age"),
      favouritePet = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.favouritePet?.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Pet?
      },
      pets = relationships?.let { safeRelationships ->
        included?.filter {
          safeRelationships.pets.data.contains(ResourceIdentifier(it.type, it.id))
        }?.map { it.original(included) }.takeIf { it?.isNotEmpty() == true } as kotlin.collections.List<com.infinum.jsonapix.`data`.models.Pet>
      } ?: throw com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException("pets"),
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
