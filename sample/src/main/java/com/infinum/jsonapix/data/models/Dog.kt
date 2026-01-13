package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import kotlinx.serialization.Serializable

@JsonApiX("dog")
@Serializable
data class Dog(
    val age: Int,
    val name: String,
) : JsonApiModel()
