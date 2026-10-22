package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.annotations.JsonApiXLinks
import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import kotlinx.serialization.Serializable

// Article has custom links and meta in every placement, and a custom error.
@JsonApiX("article")
@Serializable
data class Article(
    val title: String,
    @HasMany("tag")
    val tags: List<Tag>,
    @HasOne("comment")
    val pinnedComment: Comment?,
) : JsonApiModel()

@Serializable
@JsonApiXLinks("article")
data class ArticleRootLinks(val feed: String) : Links

@Serializable
@JsonApiXLinks("article", LinksPlacementStrategy.DATA)
data class ArticleResourceLinks(val permalink: String) : Links

@Serializable
@JsonApiXLinks(type = "article", placementStrategy = LinksPlacementStrategy.RELATIONSHIPS)
data class ArticleRelationshipsLinks(val related: String) : Links

@Serializable
@JsonApiXMeta("article", MetaPlacementStrategy.ROOT)
data class ArticleRootMeta(val totalCount: Int) : Meta

@Serializable
@JsonApiXMeta("article", MetaPlacementStrategy.DATA)
data class ArticleResourceMeta(val readingTime: Int) : Meta

@Serializable
@JsonApiXMeta(type = "article", placementStrategy = MetaPlacementStrategy.RELATIONSHIPS)
data class ArticleRelationshipsMeta(val count: Int) : Meta

@Serializable
@JsonApiXError("article")
data class ArticleError(val reason: String) : Error
