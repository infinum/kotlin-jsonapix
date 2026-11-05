package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Links
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
@SerialName(value = "Relationships_book")
public data class Relationships_Book(
  @SerialName(value = "author")
  public val author: OneRelationshipMember? = null,
) : Relationships {
  @Transient
  override val links: Map<String, Links?> = mapOf("author" to author?.links, )

  @Transient
  override val meta: Map<String, Meta?> = mapOf("author" to author?.meta, )

  public companion object {
    public fun fromOriginalObject(originalObject: Book): Relationships_Book = Relationships_Book(author = originalObject.author?.toOneRelationshipModel("author", (originalObject.author as? JsonApiModel)?.id().orEmpty()))
  }
}
