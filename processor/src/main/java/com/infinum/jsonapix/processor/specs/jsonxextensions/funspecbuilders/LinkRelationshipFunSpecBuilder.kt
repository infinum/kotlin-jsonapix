package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
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
 * api.update(OwnerModel(data = owner.linkMyFavoriteDog("1")))
 * ```
 *
 * The JSON:API type belongs in the generated code rather than at the call site, because the
 * processor is the only thing that has read it off the `@HasOne` annotation. Everything else is
 * left to [CopyModelFunSpecBuilder], which already rebuilds the model and carries its identity.
 */
internal object LinkRelationshipFunSpecBuilder {

    private const val ID_PARAMETER = "id"

    fun build(className: ClassName, property: PropertySpec): FunSpec {
        val name = property.name.replaceFirstChar { it.uppercaseChar() }

        return FunSpec.builder("link$name")
            .addKdoc(
                "Points %N at the resource with this id, without materializing it. The resource " +
                    "is not added to `included`.",
                property.name,
            )
            .receiver(className)
            .addParameter(ID_PARAMETER, String::class)
            .returns(className)
            .addStatement(
                "return %N(%N = %T.Linked(%T(%L, %N)))",
                JsonApiConstants.Members.COPY_MODEL,
                property.name,
                Relationship::class,
                ResourceIdentifier::class,
                property.relationshipType(),
                ID_PARAMETER,
            )
            .build()
    }
}
