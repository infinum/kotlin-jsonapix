package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXLinks
import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Links
import kotlinx.serialization.Serializable

// Tag only has custom root links.
@JsonApiX("tag")
@Serializable
data class Tag(val label: String) : JsonApiModel()

@Serializable
@JsonApiXLinks("tag", LinksPlacementStrategy.ROOT)
data class TagLinks(val search: String) : Links
