package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.JsonApiSerializable
import kotlinx.serialization.Serializable

@Serializable
@JsonApiSerializable("person")
data class Person(
    val name: String,
    val surname: String
)
