package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ManyRelationshipMember(
    @SerialName("data") val data: List<ResourceIdentifier>,
    @SerialName("links") val links: Links? = null
)
