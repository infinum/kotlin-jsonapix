package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Relationship
import kotlinx.serialization.Serializable

/**
 * Carries both to-one shapes and a to-many at once, which is what a half finished migration looks
 * like. The nullable [legacyDog] must keep producing an omitted member while [wrappedDog] is free
 * to produce `{"data": null}`.
 */
@Serializable
@JsonApiX("breeder")
data class Breeder(
    val name: String,
    @HasOne("dog")
    val legacyDog: Dog?,
    @HasOne("dog")
    val wrappedDog: Relationship<Dog> = Relationship.Unset,
    @HasMany("dog")
    val allDogs: List<Dog>?,
) : JsonApiModel()

/**
 * Declares properties called `id` and `type`, the two names that collide with the members every
 * resource object has and with the generated link helper's own parameter. They are the model's own
 * attributes and must stay out of the resource object's identity.
 */
@Serializable
@JsonApiX("registration")
data class Registration(
    val id: String,
    val type: String,
    @HasOne("dog")
    val dog: Relationship<Dog> = Relationship.Unset,
) : JsonApiModel()
