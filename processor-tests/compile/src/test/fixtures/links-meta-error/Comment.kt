package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Error
import kotlinx.serialization.Serializable

// Comment only has a custom error.
@JsonApiX("comment")
@Serializable
data class Comment(val body: String) : JsonApiModel()

@Serializable
@JsonApiXError("comment")
data class CommentError(val field: String) : Error
