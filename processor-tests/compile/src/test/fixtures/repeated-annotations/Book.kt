package com.infinum.jsonapix.data.models

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

// Book repeats one links class and one meta class in every placement, and shares its error class
// with Author. The annotations are repeated directly, the way consumers write them.
@JsonApiX("book")
@Serializable
data class Book(
    val title: String,
    @HasOne("author")
    val author: Author?,
) : JsonApiModel()

@Serializable
@JsonApiXLinks("book")
@JsonApiXLinks("book", LinksPlacementStrategy.DATA)
@JsonApiXLinks(type = "book", placementStrategy = LinksPlacementStrategy.RELATIONSHIPS)
data class BookLinks(val self: String) : Links

@Serializable
@JsonApiXMeta("book", MetaPlacementStrategy.ROOT)
@JsonApiXMeta("book", MetaPlacementStrategy.DATA)
@JsonApiXMeta(type = "book", placementStrategy = MetaPlacementStrategy.RELATIONSHIPS)
data class BookMeta(val count: Int) : Meta

@Serializable
@JsonApiXError("book")
@JsonApiXError("author")
data class LibraryError(val reason: String) : Error
