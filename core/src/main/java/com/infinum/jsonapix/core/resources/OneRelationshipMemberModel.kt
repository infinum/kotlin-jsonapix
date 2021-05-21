package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OneRelationshipMemberModel(
    @SerialName("data") val data: ResourceIdentifier,
    @SerialName("links") val links: LinksModel? = null
)