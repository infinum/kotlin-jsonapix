package com.infinum.jsonapix.core.resources

import kotlinx.serialization.Serializable

@Serializable
class ResourceIdentifier(
    val type: String,
    val id: String = "0"
)
