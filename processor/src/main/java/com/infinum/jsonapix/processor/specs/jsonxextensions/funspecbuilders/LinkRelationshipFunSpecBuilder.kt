package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.resources.Relationship
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.processor.extensions.relationshipType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
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

    private const val ID_PARAMETER = "id"

    /**
     * @param className the annotated model.
     * @param property the `Relationship<T>` property this helper points.
     * @param allProperties every property of the model, in any order, since the generated call
     * names its arguments.
     */
    fun build(className: ClassName, property: PropertySpec, allProperties: List<PropertySpec>): FunSpec {
        val name = property.name.replaceFirstChar { it.uppercaseChar() }

        return FunSpec.builder("link$name")
            .addKdoc(
                "Points %N at the resource with this id, without materializing it. The resource " +
                    "is not added to `included`.\n\nThe returned copy keeps the type and id of " +
                    "the receiver, which a plain constructor call drops.",
                property.name,
            )
            .receiver(className)
            .addParameter(ID_PARAMETER, String::class)
            .returns(className)
            .addStatement(
                "return %T(%L).also { it.setType(this.type()); it.setId(this.id()) }",
                className,
                constructorArguments(property, allProperties),
            )
            .build()
    }

    /**
     * Rebuilds the model through its constructor rather than through `copy`, so that a model which
     * is not a data class is supported too. This is the same call the generated `original()` makes.
     *
     * Every carried over property is read as `this.<name>`, because the model is free to declare a
     * property called `id`, which would otherwise bind to this helper's own parameter.
     */
    private fun constructorArguments(property: PropertySpec, allProperties: List<PropertySpec>): CodeBlock {
        val arguments = CodeBlock.builder()

        allProperties.forEachIndexed { index, current ->
            if (current.name == property.name) {
                arguments.add(
                    "%N = %T.Linked(%T(%L, %N))",
                    current.name,
                    Relationship::class,
                    ResourceIdentifier::class,
                    property.relationshipType(),
                    ID_PARAMETER,
                )
            } else {
                arguments.add("%N = this.%N", current.name, current.name)
            }

            if (index != allProperties.lastIndex) {
                arguments.add(", ")
            }
        }

        return arguments.build()
    }
}
