package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

internal object WrapperListFunSpecBuilder {

    fun build(
        originalClass: ClassName,
        wrapperClass: ClassName,
        includedListStatement: String?,
        isNullable: Boolean,
    ): FunSpec {
        val modelClass = ClassName.bestGuess(originalClass.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST))

        val builderArgs =
            mutableListOf<Any>(wrapperClass)

        val returnStatement = if (isNullable) {
            StringBuilder(
                "return %T(data =data?.map { it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }?.filterNotNull().orEmpty()"
            )
        } else {
            StringBuilder(
                "return %T(data =data.map { it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }"
            )
        }

        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_LIST_GETTER)
            .receiver(modelClass)
            .returns(wrapperClass)
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray()
            )
            .build()
    }
}
