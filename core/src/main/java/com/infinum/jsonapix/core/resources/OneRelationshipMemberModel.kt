package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OneRelationshipMemberModel(
    @SerialName("links") val links: LinksModel?,
    @SerialName("data") val data: ResourceIdentifier
)