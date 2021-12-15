package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@JsonApiX("dog")
@Serializable
data class Dog(val name: String, val age: Int)
