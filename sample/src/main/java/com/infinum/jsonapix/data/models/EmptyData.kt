package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Meta
import kotlinx.serialization.Serializable

@JsonApiX("emptyData", isNullable = true)
@Serializable
class EmptyData : JsonApiModel()

@Serializable
@JsonApiXMeta("emptyData")
data class EmptyDataMeta(val owner: String) : Meta