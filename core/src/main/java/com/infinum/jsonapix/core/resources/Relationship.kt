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
 * Declaring the property as `T?` stays supported and keeps its current wire output: the member is
 * left out for `null`, and carries linkage plus an `included` entry otherwise. That shape produces
 * none of the states below, which is why it cannot ask for [Unlink]. It is legacy and will be
 * dropped in the next major release.
 *
 * A property declared `Relationship<T>?` is accepted as well, where `null` reads as [Unset]. It
 * says "leave this relationship alone" twice over, so prefer the non-nullable declaration.
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
     * Remove whatever this relationship points at, the opposite of [Linked].
     *
     * Writes `{"data": null}`, which JSON:API defines as removing the relationship. A server is
     * not allowed to read an absent member that way, so [Unset] and [Unlink] are not
     * interchangeable: one leaves the relationship as it is, the other empties it.
     */
    @Serializable
    data object Unlink : Relationship<Nothing>

    /**
     * The relationship points at a resource that has not been materialized.
     *
     * On the way out this writes linkage and adds nothing to `included`, which is how a request
     * points at a resource the caller never fetched. On the way in it carries linkage that arrived
     * without a matching entry in `included`, which a nullable property has no way to express and
     * so discards.
     *
     * @property identifier the type and id the relationship points at.
     */
    @Serializable
    data class Linked(val identifier: ResourceIdentifier) : Relationship<Nothing>

    /**
     * The relationship points at [value], and the resource itself is present. Writes linkage and
     * puts the resource in `included`.
     *
     * @param T the type of the related model.
     * @property value the related model.
     */
    @Serializable
    data class Included<out T>(val value: T) : Relationship<T>
}

/**
 * The related model when this relationship is a [Relationship.Included], and `null` for every
 * other state.
 *
 * This is the counterpart of reading a nullable `@HasOne` property, and collapses the states the
 * same way: a caller that only wants the model cannot tell [Relationship.Unset],
 * [Relationship.Unlink] and [Relationship.Linked] apart. Match on the relationship itself where
 * that distinction matters.
 */
val <T> Relationship<T>.value: T?
    get() = (this as? Relationship.Included)?.value
