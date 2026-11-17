package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Relationship
import kotlinx.serialization.Serializable

/**
 * Declares its to-one relationship as a [Relationship], which is the shape that can tell "leave
 * this relationship alone" apart from "clear it". [Person] keeps the nullable shape, so the two
 * live side by side in the sample.
 */
@Serializable
@JsonApiX("owner")
data class Owner(
    val name: String,
    @HasOne("dog")
    val myFavoriteDog: Relationship<Dog> = Relationship.Unset,
) : JsonApiModel()
