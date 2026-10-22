package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Attributes
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
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "ResourceObject_company")
public class ResourceObject_Company(
  @SerialName(value = "type")
  override val type: String = "company",
  @SerialName(value = "id")
  override val id: String = "0",
  @Transient
  override val attributes: Attributes? = null,
  @SerialName(value = "relationships")
  override val relationships: Relationships_Company? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: Meta? = null,
) : ResourceObject<Company> {
  override fun original(included: List<ResourceObject<Any>>?): Company {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Company(
      manager = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.manager.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Person
      } ?: throw com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException("manager"),
      address = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.address.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Address
      } ?: throw com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException("address"),
      personel = relationships?.let { safeRelationships ->
        included?.filter {
          safeRelationships.personel.data.contains(ResourceIdentifier(it.type, it.id))
        }?.map { it.original(included) }.takeIf { it?.isNotEmpty() == true } as kotlin.collections.List<com.infinum.jsonapix.`data`.models.Person>
      } ?: throw com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException("personel"),
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
