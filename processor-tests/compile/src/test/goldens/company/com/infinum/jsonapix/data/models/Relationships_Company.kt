package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.toManyRelationshipModel
import com.infinum.jsonapix.toOneRelationshipModel
import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "Relationships_company")
public data class Relationships_Company(
  @SerialName(value = "manager")
  public val manager: OneRelationshipMember,
  @SerialName(value = "address")
  public val address: OneRelationshipMember,
  @SerialName(value = "personel")
  public val personel: ManyRelationshipMember,
) : Relationships {
  @Transient
  override val links: Map<String, Links?> =
      mapOf("manager" to manager.links, "address" to address.links, "personel" to personel.links, )

  @Transient
  override val meta: Map<String, Meta?> =
      mapOf("manager" to manager.meta, "address" to address.meta, "personel" to personel.meta, )

  public companion object {
    public fun fromOriginalObject(originalObject: Company): Relationships_Company = Relationships_Company(manager = originalObject.manager.toOneRelationshipModel("person", (originalObject.manager as? JsonApiModel)?.id().orEmpty()), address = originalObject.address.toOneRelationshipModel("address", (originalObject.address as? JsonApiModel)?.id().orEmpty()), personel = originalObject.personel.toManyRelationshipModel("person", { (it as? JsonApiModel)?.id().orEmpty() }))
  }
}
