package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A to-many relationship object.
 *
 * JSON:API requires a relationship object to carry at least one of [links], [data] or [meta], so [data] is
 * optional. A null [data] means the server did not provide the related resources here, which is different
 * from an empty list: that one means the resource genuinely has no related resources.
 */
@Suppress("UseDataClass")
@Serializable
class ManyRelationshipMember(
    @SerialName("data") val data: List<ResourceIdentifier>? = null,
    @SerialName("links") val links: Links? = null,
    @SerialName("meta") val meta: Meta? = null,
)
