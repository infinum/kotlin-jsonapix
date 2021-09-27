package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OneRelationshipMember(
    @SerialName("data") val data: ResourceIdentifier,
    @SerialName("links") val links: Links? = null
)
