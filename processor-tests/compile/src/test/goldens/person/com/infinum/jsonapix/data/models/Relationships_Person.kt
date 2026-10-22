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
@SerialName(value = "Relationships_person")
public data class Relationships_Person(
  @SerialName(value = "myFavoriteDog")
  public val myFavoriteDog: OneRelationshipMember? = null,
  @SerialName(value = "allMyDogs")
  public val allMyDogs: ManyRelationshipMember? = null,
) : Relationships {
  @Transient
  override val links: Map<String, Links?> =
      mapOf("myFavoriteDog" to myFavoriteDog?.links, "allMyDogs" to allMyDogs?.links, )

  @Transient
  override val meta: Map<String, Meta?> =
      mapOf("myFavoriteDog" to myFavoriteDog?.meta, "allMyDogs" to allMyDogs?.meta, )

  public companion object {
    public fun fromOriginalObject(originalObject: Person): Relationships_Person = Relationships_Person(myFavoriteDog = originalObject.myFavoriteDog?.toOneRelationshipModel("dog", (originalObject.myFavoriteDog as? JsonApiModel)?.id().orEmpty()), allMyDogs = originalObject.allMyDogs?.toManyRelationshipModel("dog", { (it as? JsonApiModel)?.id().orEmpty() }))
  }
}
