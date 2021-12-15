package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@JsonApiX(type = "address")
@Serializable
data class Address(
    val street: String,
    val number: Int,
    val country: String,
    val city: String
)