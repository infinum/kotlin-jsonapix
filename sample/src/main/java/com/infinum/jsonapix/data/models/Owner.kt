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

/**
 * Declares the same relationship as a nullable [Relationship]. The declaration says "leave this
 * alone" twice over, once as `null` and once as [Relationship.Unset], and both read that way.
 * [Owner] is the shape to copy.
 */
@Serializable
@JsonApiX("shelter")
data class Shelter(
    val name: String,
    @HasOne("dog")
    val featuredDog: Relationship<Dog>? = null,
) : JsonApiModel()

/**
 * A plain class rather than a data class. JsonApiX never needed `data`, and the generated link
 * helper rebuilds the model through its constructor rather than through `copy`, so a model like
 * this gets one too.
 */
@Serializable
@JsonApiX("kennel")
class Kennel(
    val name: String,
    @HasOne("dog")
    val residentDog: Relationship<Dog> = Relationship.Unset,
) : JsonApiModel()
