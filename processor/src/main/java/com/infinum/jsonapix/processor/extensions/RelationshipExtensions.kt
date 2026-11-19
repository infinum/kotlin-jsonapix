package com.infinum.jsonapix.processor.extensions

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.core.resources.Relationship
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

private val relationshipClassName = Relationship::class.asClassName()

/**
 * The related model's type when this is a `Relationship<T>`, and `null` for the legacy nullable
 * form. Every branch in the generated code keys off this: the declared type of a `@HasOne`
 * property is what decides which of the two shapes a model opted into.
 */
internal fun TypeName.relationshipValueType(): TypeName? =
    (this as? ParameterizedTypeName)
        ?.takeIf { it.rawType == relationshipClassName }
        ?.typeArguments
        ?.firstOrNull()

/**
 * Whether this `@HasOne` property is declared as `Relationship<T>` rather than as a nullable model.
 */
internal val PropertySpec.isRelationshipWrapper: Boolean
    get() = type.relationshipValueType() != null

/**
 * Whether the member generated for this relationship is nullable.
 *
 * A nullable declared type makes it nullable, and so does `Relationship<T>`: the member has to be
 * droppable for [Relationship.Unset], which is written by leaving it out of the document.
 */
internal val PropertySpec.hasNullableMember: Boolean
    get() = type.isNullable || isRelationshipWrapper

/**
 * The JSON:API type this relationship points at, as the `@HasOne` or `@HasMany` annotation spells
 * it. The value is already a quoted string literal, ready to drop into generated code.
 */
internal fun PropertySpec.relationshipType(): String =
    annotations.first { annotation ->
        annotation.typeName == HasOne::class.asTypeName() ||
            annotation.typeName == HasMany::class.asTypeName()
    }.members.first { member ->
        member.toString().trim().startsWith("type")
    }.toString().split("=")[1].trim()
