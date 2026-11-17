package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Errors<out ErrorModel : Error>(
    @SerialName("errors") val errors: List<ErrorModel>,
)

@Serializable
data class DefaultErrors(
    @SerialName("errors") val errors: List<DefaultError>,
)
