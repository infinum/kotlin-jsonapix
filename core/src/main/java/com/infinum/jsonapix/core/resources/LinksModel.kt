package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("links")
class LinksModel(
    @SerialName("self") val self: String? = null,
    @SerialName("related") val related: String? = null,
    @SerialName("first") val first: String? = null,
    @SerialName("last") val last: String? = null,
    @SerialName("next") val next: String? = null,
    @SerialName("prev") val prev: String? = null
)
