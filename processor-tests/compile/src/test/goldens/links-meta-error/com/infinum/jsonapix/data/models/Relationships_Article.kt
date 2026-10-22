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
@SerialName(value = "Relationships_article")
public data class Relationships_Article(
  @SerialName(value = "pinnedComment")
  public val pinnedComment: OneRelationshipMember? = null,
  @SerialName(value = "tags")
  public val tags: ManyRelationshipMember,
) : Relationships {
  @Transient
  override val links: Map<String, Links?> =
      mapOf("pinnedComment" to pinnedComment?.links, "tags" to tags.links, )

  @Transient
  override val meta: Map<String, Meta?> =
      mapOf("pinnedComment" to pinnedComment?.meta, "tags" to tags.meta, )

  public companion object {
    public fun fromOriginalObject(originalObject: Article): Relationships_Article = Relationships_Article(pinnedComment = originalObject.pinnedComment?.toOneRelationshipModel("comment", (originalObject.pinnedComment as? JsonApiModel)?.id().orEmpty()), tags = originalObject.tags.toManyRelationshipModel("tag", { (it as? JsonApiModel)?.id().orEmpty() }))
  }
}
