package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.requireNotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "ResourceObject_person")
public class ResourceObject_Person(
  @SerialName(value = "type")
  override val type: String = "person",
  @SerialName(value = "id")
  override val id: String = "0",
  @SerialName(value = "attributes")
  override val attributes: Attributes_Person? = null,
  @SerialName(value = "relationships")
  override val relationships: Relationships_Person? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: PersonResourceMeta? = null,
) : ResourceObject<Person> {
  override fun original(included: List<ResourceObject<Any>>?): Person {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Person(
      age = requireNotNull(attributes?.age, "age"),
      name = attributes?.name,
      surname = requireNotNull(attributes?.surname, "surname"),
      myFavoriteDog = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.myFavoriteDog?.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Dog?
      },
      allMyDogs = relationships?.let { safeRelationships ->
        included?.filter {
          safeRelationships.allMyDogs?.data?.contains(ResourceIdentifier(it.type, it.id)) == true
        }?.map { it.original(included) }.takeIf { it?.isNotEmpty() == true } as kotlin.collections.List<com.infinum.jsonapix.`data`.models.Dog>?
      },
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
