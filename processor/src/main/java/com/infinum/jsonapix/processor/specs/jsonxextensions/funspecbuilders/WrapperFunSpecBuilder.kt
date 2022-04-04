package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal object WrapperFunSpecBuilder {

    fun buildSingle(
        originalClass: ClassName,
        wrapperClass: ClassName,
        includedListStatement: String?
    ): FunSpec {
        val builderArgs =
            mutableListOf<Any>(wrapperClass)
        val returnStatement = StringBuilder(
            "return %T(data = this.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}()"
        )

        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_GETTER)
            .receiver(originalClass)
            .returns(wrapperClass)
            .addStatement(
                returnStatement.toString(),
                *builderArgs.toTypedArray()
            )
            .build()
    }

    fun buildList(
        originalClass: ClassName,
        wrapperClass: ClassName,
        includedListStatement: String?
    ): FunSpec {
        val builderArgs =
            mutableListOf<Any>(wrapperClass)
        val returnStatement = StringBuilder(
            "return %T(data = map { it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }"
        )

        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_LIST_GETTER)
            .receiver(Iterable::class.asClassName().parameterizedBy(originalClass))
            .returns(wrapperClass)
            .addStatement(
                returnStatement.toString(),
                *builderArgs.toTypedArray()
            )
            .build()
    }
}