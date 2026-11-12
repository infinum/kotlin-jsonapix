package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("error")
class DefaultError(
    @SerialName("code") val code: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("detail") val detail: String? = null,
    @SerialName("status") val status: String? = null,
) : Error
