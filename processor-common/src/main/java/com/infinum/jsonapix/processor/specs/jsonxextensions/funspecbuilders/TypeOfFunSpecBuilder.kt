package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

/**
 * Builds the lookup that the generic decode functions use when a document has no data to read the
 * type from. It is published API because those functions are public and inline.
 */
internal object TypeOfFunSpecBuilder {

    private const val CLASS_PARAMETER = "kClass"
    private const val MISSING_TYPE_MESSAGE = "has no JSON:API type. " +
        "Only @JsonApiX classes and their generated Model, List and Item classes have one."

    fun build(): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.JSONX_TYPE_OF)
            .addAnnotation(PublishedApi::class)
            .addModifiers(KModifier.INTERNAL)
            .addParameter(CLASS_PARAMETER, KClass::class.asClassName().parameterizedBy(STAR))
            .returns(String::class)
            .addStatement(
                "return %N[%N] ?: throw %T(\"\${%N.simpleName} $MISSING_TYPE_MESSAGE\")",
                JsonApiConstants.Members.JSONX_TYPES,
                CLASS_PARAMETER,
                IllegalArgumentException::class.asClassName(),
                CLASS_PARAMETER,
            )
            .build()
    }
}
