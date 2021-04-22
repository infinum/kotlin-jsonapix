package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@Serializable
@JsonApiX("person")
data class Person(
    val name: String,
    val surname: String,
    val age: Int
)
