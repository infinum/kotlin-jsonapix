package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.JsonApiSerializable
import kotlinx.serialization.Serializable

@JsonApiSerializable("dog")
@Serializable
data class Dog(val name: String, val age: Int)
