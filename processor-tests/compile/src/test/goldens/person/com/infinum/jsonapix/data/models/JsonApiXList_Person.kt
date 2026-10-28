package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiXList
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
public class JsonApiXList_Person(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<PersonalError>? = null,
  @SerialName(value = "links")
  override val links: DefaultLinks? = null,
  @SerialName(value = "meta")
  override val meta: PersonRootMeta? = null,
  @SerialName(value = "data")
  override val `data`: List<ResourceObject_Person>,
) : JsonApiXList<Person, PersonList> {
  @Transient
  override val original: PersonList
    get() {
      val items = data.map {
      val original = it.original(included)
      PersonItem(
          original,
          it.links,
          it.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? DefaultLinks },
          it.meta,
          it.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? PersonRelationshipMeta } )
      }
      return PersonList(items, links, errors, meta)
    }
}
