package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Owner has @SerialName on attributes and on one and many relationships, placed before and after
// the relationship annotation. Its age attribute has no @SerialName.
@JsonApiX("owner")
@Serializable
data class Owner(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String?,
    val age: Int,
    @HasOne("pet")
    @SerialName("favourite_pet")
    val favouritePet: Pet?,
    @SerialName("all_pets")
    @HasMany("pet")
    val pets: List<Pet>,
) : JsonApiModel()
