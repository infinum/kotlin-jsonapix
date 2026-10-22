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
@SerialName(value = "ResourceObject_article")
public class ResourceObject_Article(
  @SerialName(value = "type")
  override val type: String = "article",
  @SerialName(value = "id")
  override val id: String = "0",
  @SerialName(value = "attributes")
  override val attributes: Attributes_Article? = null,
  @SerialName(value = "relationships")
  override val relationships: Relationships_Article? = null,
  @SerialName(value = "links")
  override val links: ArticleResourceLinks? = null,
  @SerialName(value = "meta")
  override val meta: ArticleResourceMeta? = null,
) : ResourceObject<Article> {
  override fun original(included: List<ResourceObject<Any>>?): Article {
    val tempOriginal = com.infinum.jsonapix.`data`.models.Article(
      title = requireNotNull(attributes?.title, "title"),
      pinnedComment = relationships?.let { safeRelationships ->
        included?.firstOrNull {
          safeRelationships.pinnedComment?.data == ResourceIdentifier(it.type, it.id)
        }?.original(included) as com.infinum.jsonapix.`data`.models.Comment?
      },
      tags = relationships?.let { safeRelationships ->
        included?.filter {
          safeRelationships.tags.data.contains(ResourceIdentifier(it.type, it.id))
        }?.map { it.original(included) }.takeIf { it?.isNotEmpty() == true } as kotlin.collections.List<com.infinum.jsonapix.`data`.models.Tag>
      } ?: throw com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException("tags"),
      )
      (tempOriginal as? JsonApiModel)?.let {
        tempOriginal.setType(type)
        tempOriginal.setId(id)
      }
      return tempOriginal
  }
}
