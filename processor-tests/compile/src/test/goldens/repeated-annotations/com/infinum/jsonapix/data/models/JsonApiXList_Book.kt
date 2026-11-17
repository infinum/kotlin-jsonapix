package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName(value = "book")
public class JsonApiXList_Book(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<LibraryError>? = null,
  @SerialName(value = "links")
  override val links: BookLinks? = null,
  @SerialName(value = "meta")
  override val meta: BookMeta? = null,
  @SerialName(value = "data")
  override val `data`: List<ResourceObject_Book>,
) : JsonApiXList<Book, BookList> {
  @Transient
  override val original: BookList
    get() {
      val items = data.map {
      val original = it.original(included)
      BookItem(
          original,
          it.links,
          it.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? BookLinks },
          it.meta,
          it.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? BookMeta } )
      }
      return BookList(items, links, errors, meta)
    }
}
