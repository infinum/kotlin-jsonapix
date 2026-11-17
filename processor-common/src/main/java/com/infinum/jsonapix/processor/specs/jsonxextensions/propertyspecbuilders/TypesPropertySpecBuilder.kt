package com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

/**
 * Builds the table that maps each model class and its generated wrappers (Model, List and Item)
 * to the model's JSON:API type, so generated code never reads @JsonApiX at runtime.
 */
internal object TypesPropertySpecBuilder {

    fun build(types: Map<ClassName, String>): PropertySpec {
        val codeBlockBuilder = CodeBlock.builder()
            .addStatement("mapOf(")
            .indent()

        types.forEach { (className, type) ->
            val classNames = listOf(
                className,
                ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL)),
                ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST)),
                ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST_ITEM)),
            )
            classNames.forEach { codeBlockBuilder.addStatement("%T::class to %S,", it, type) }
        }

        codeBlockBuilder
            .unindent()
            .addStatement(")")

        return PropertySpec.builder(
            JsonApiConstants.Members.JSONX_TYPES,
            Map::class.asClassName().parameterizedBy(
                KClass::class.asClassName().parameterizedBy(STAR),
                String::class.asClassName(),
            ),
        )
            .addModifiers(KModifier.PRIVATE)
            .initializer(codeBlockBuilder.build())
            .build()
    }
}
