package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.resources.Relationship
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.processor.extensions.relationshipType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Builds the per property helper that points a `Relationship<T>` at a resource by id:
 *
 * ```kotlin
 * api.update(owner.linkMyFavoriteDog("1"))
 * ```
 *
 * The JSON:API type belongs in the generated code rather than at the call site, because the
 * processor is the only thing that has read it off the `@HasOne` annotation.
 */
internal object LinkRelationshipFunSpecBuilder {

    fun build(className: ClassName, property: PropertySpec): FunSpec {
        val name = property.name.replaceFirstChar { it.uppercaseChar() }

        return FunSpec.builder("link$name")
            .addKdoc(
                "Points %N at the resource with this id, without materializing it. The resource " +
                    "is not added to `included`.\n\nThe returned copy keeps the type and id of " +
                    "the receiver, which `copy` on its own drops.",
                property.name,
            )
            .receiver(className)
            .addParameter("id", String::class)
            .returns(className)
            .addStatement(
                "return copy(%N = %T.Linked(%T(%L, id))).also { it.setType(this.type()); it.setId(this.id()) }",
                property.name,
                Relationship::class,
                ResourceIdentifier::class,
                property.relationshipType(),
            )
            .build()
    }
}
