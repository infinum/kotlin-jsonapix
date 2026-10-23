package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import kotlinx.serialization.Serializable

@JsonApiX("pet")
@Serializable
data class Pet(val name: String) : JsonApiModel()
