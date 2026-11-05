package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import kotlinx.serialization.Serializable

// Author only has the error class it shares with Book.
@JsonApiX("author")
@Serializable
data class Author(val name: String) : JsonApiModel()
