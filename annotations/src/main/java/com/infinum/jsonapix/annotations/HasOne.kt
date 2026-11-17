package com.infinum.jsonapix.annotations

/**
 * Marks a property as a to-one relationship.
 *
 * The declared type of the property decides how much the relationship can say:
 *
 * ```kotlin
 * @HasOne("dog") val dog: Dog?                 // legacy, null means "leave this alone"
 * @HasOne("dog") val dog: Relationship<Dog>    // Unset, Clear, Linked and To
 * ```
 *
 * A nullable property cannot tell "leave this relationship alone" apart from "clear it", because
 * JSON:API spells those as an absent member and as `{"data": null}` and a single null collapses
 * both. Declaring the property as `com.infinum.jsonapix.core.resources.Relationship` separates
 * them. The nullable form keeps its current behaviour and wire output, and will be dropped in the
 * next major release.
 *
 * @property type the JSON:API type of the related resource. It must match the type its class
 * declares in its own `@JsonApiX`.
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasOne(val type: String)
