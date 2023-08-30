package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

internal object WrapperFunSpecBuilder {

    fun build(
        originalClass: ClassName,
        wrapperClass: ClassName,
        includedListStatement: String?,
        isNullable: Boolean,
    ): FunSpec {
        val modelClass = ClassName.bestGuess(originalClass.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL))

        val builderArgs =
            mutableListOf<Any>(wrapperClass)

        val dataStatement = if(isNullable) "this.data?" else "this.data"
        val returnStatement = StringBuilder(
            "return %T(data = $dataStatement.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}()",
        )

        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }
        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_GETTER)
            .receiver(modelClass)
            .returns(wrapperClass)
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray()
            )
            .build()
    }
}
