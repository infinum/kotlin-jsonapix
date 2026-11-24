package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Builds the identity preserving counterpart of `copy`:
 *
 * ```kotlin
 * api.update(OwnerModel(data = owner.copyModel(myFavoriteDog = Relationship.Unlink)))
 * ```
 *
 * `copy` cannot be used for this. A model's `type` and `id` live in private fields on
 * `JsonApiModel` and are set after construction, so they are not constructor parameters and `copy`
 * leaves them behind. The copy then serializes with the `id() ?: "0"` fallback and the request goes
 * out claiming to be resource `0`, which is a wrong value rather than a failure.
 *
 * `copy` belongs to the Kotlin compiler and cannot be changed or overridden, and an extension named
 * `copy` would be shadowed by the member and silently never run. This is generated under its own
 * name instead, close enough to `copy` that it surfaces beside it in completion.
 *
 * It is generated for every annotated model, because the problem is not specific to relationships:
 * changing an attribute through `copy` loses the identity the same way. A model that is not a data
 * class has no `copy` at all and gets this as its only option.
 */
internal object CopyModelFunSpecBuilder {

    fun build(className: ClassName, allProperties: List<PropertySpec>): FunSpec {
        val builder = FunSpec.builder(JsonApiConstants.Members.COPY_MODEL)
            .addKdoc(
                "Returns a copy of this %N carrying the same JSON:API `type` and `id`.\n\n" +
                    "Prefer this over `copy`, which drops both and makes the copy serialize as " +
                    "resource `0`.",
                className.simpleName,
            )
            .receiver(className)
            .returns(className)

        allProperties.forEach { property ->
            builder.addParameter(
                ParameterSpec.builder(property.name, property.type)
                    .defaultValue("this.%N", property.name)
                    .build(),
            )
        }

        return builder
            .addStatement(
                "return %T(%L).also { it.setType(this.type()); it.setId(this.id()) }",
                className,
                constructorArguments(allProperties),
            )
            .build()
    }

    private fun constructorArguments(allProperties: List<PropertySpec>): CodeBlock {
        val arguments = CodeBlock.builder()

        allProperties.forEachIndexed { index, property ->
            arguments.add("%N = %N", property.name, property.name)

            if (index != allProperties.lastIndex) {
                arguments.add(", ")
            }
        }

        return arguments.build()
    }
}
