package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "person")
public class JsonApiX_Person(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<PersonalError>? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: PersonRootMeta? = null,
  @SerialName(value = "data")
  override val `data`: ResourceObject_Person,
) : JsonApiX<Person, PersonModel> {
  @Transient
  override val original: PersonModel
    get() {
      val original = data?.original(included)!!
      val model = PersonModel(
          original,
          links,
          data?.links,
          data?.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? DefaultLinks },
          errors,
          meta,
          data?.meta,
          data?.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? PersonRelationshipMeta } )
      return model
    }
}
