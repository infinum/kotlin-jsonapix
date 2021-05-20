package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@JsonApiX(type = "company")
@Serializable
data class Company(
    @HasMany
    val personel: List<Person>
)
