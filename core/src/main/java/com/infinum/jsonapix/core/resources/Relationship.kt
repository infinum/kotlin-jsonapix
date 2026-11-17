package com.infinum.jsonapix.core.resources

import kotlinx.serialization.Serializable

/**
 * The state of a to-one relationship on a model.
 *
 * JSON:API treats three things as distinct in a request document, and a single nullable property
 * cannot express all three. Declaring a `@HasOne` property as `Relationship<T>` instead of `T?`
 * opts that property into the full set:
 *
 * ```kotlin
 * @JsonApiX("person")
 * @Serializable
 * data class Person(
 *     val name: String,
 *     @HasOne("dog")
 *     val myFavoriteDog: Relationship<Dog> = Relationship.Unset,
 * ) : JsonApiModel()
 * ```
 *
 * The nullable form stays supported and keeps its current behaviour, which is [Unset] for `null`
 * and [To] otherwise. It is legacy and will be dropped in the next major release.
 *
 * @param T the type of the related model.
 */
@Serializable
sealed interface Relationship<out T> {

    /**
     * The relationship is not part of this request. Its member is left out of `relationships`
     * entirely, which JSON:API defines as "leave this relationship as it is".
     *
     * This is the default, and what a nullable `@HasOne` property produces for `null`.
     */
    @Serializable
    data object Unset : Relationship<Nothing>

    /**
     * Clear the relationship. Writes `{"data": null}`, which JSON:API defines as removing the
     * relationship. A server is not allowed to read an absent member this way, so [Unset] and
     * [Clear] are not interchangeable.
     */
    @Serializable
    data object Clear : Relationship<Nothing>

    /**
     * The relationship points at a resource that has not been materialized.
     *
     * On the way out this writes linkage and adds nothing to `included`, which is how a request
     * points at a resource the caller never fetched. On the way in it carries linkage that arrived
     * without a matching entry in `included`, which a nullable property has no way to express and
     * so discards.
     *
     * @property id the type and id the relationship points at.
     */
    @Serializable
    data class Linked(val id: ResourceIdentifier) : Relationship<Nothing>

    /**
     * The relationship points at [value], and the resource itself is present. Writes linkage and
     * puts the resource in `included`.
     *
     * @param T the type of the related model.
     * @property value the related model.
     */
    @Serializable
    data class To<out T>(val value: T) : Relationship<T>
}

/**
 * The related model when this relationship is a [Relationship.To], and `null` for every other
 * state.
 *
 * This is the counterpart of reading a nullable `@HasOne` property, and collapses the states the
 * same way: a caller that only wants the model cannot tell [Relationship.Unset],
 * [Relationship.Clear] and [Relationship.Linked] apart. Match on the relationship itself where
 * that distinction matters.
 */
val <T> Relationship<T>.value: T?
    get() = (this as? Relationship.To)?.value
