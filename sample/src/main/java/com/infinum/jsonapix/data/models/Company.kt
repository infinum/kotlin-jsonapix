package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@JsonApiX(type = "company")
@Serializable
data class Company(
    @HasMany("person")
    val personel: List<Person>,
    @HasOne("person")
    val manager: Person,
    @HasOne("address")
    val address: Address
)
