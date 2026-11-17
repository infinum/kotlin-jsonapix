package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiX
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
public class JsonApiX_Book(
  @SerialName(value = "included")
  override val included: List<ResourceObject<@Contextual Any>>? = null,
  @SerialName(value = "errors")
  override val errors: List<LibraryError>? = null,
  @SerialName(value = "links")
  override val links: BookLinks? = null,
  @SerialName(value = "meta")
  override val meta: BookMeta? = null,
  @SerialName(value = "data")
  override val `data`: ResourceObject_Book,
) : JsonApiX<Book, BookModel> {
  @Transient
  override val original: BookModel
    get() {
      val original = data?.original(included)!!
      val model = BookModel(
          original,
          links,
          data?.links,
          data?.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? BookLinks },
          errors,
          meta,
          data?.meta,
          data?.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? BookMeta } )
      return model
    }
}
