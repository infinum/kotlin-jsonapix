package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.requireNotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "ResourceObject_book")
public class ResourceObject_Book(
  @SerialName(value = "type")
  override val type: String = "book",
  @SerialName(value = "id")
  override val id: String = "0",
  @SerialName(value = "attributes")
  override val attributes: Attributes_Book? = null,
  @SerialName(value = "relationships")
  override val relationships: Relationships_Book? = null,
  @SerialName(value = "links")
  override val links: BookLinks? = null,
  @SerialName(value = "meta")
  override val meta: BookMeta? = null,
) : ResourceObject<Book> {
  override fun original(included: List<ResourceObject<Any>>?): Book {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Book(
      title = requireNotNull(attributes?.title, "title"),
      author = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.author?.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Author?
      },
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
