package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@JsonApiX(type = "company")
@Serializable
data class Company(
    @HasMany("person")
    val personel: List<Person>
)
