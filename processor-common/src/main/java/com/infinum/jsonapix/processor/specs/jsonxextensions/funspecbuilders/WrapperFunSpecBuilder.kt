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
        val modelClass = ClassName.bestGuess(
            originalClass.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL),
        )

        val builderArgs =
            mutableListOf<Any>(wrapperClass)

        // A nullable model without data is written as "data": null, not as a resource object.
        val dataStatement = if (isNullable) {
            "${JsonApiConstants.Keys.DATA}?.let { ${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }"
        } else {
            "${JsonApiConstants.Members.TO_RESOURCE_OBJECT}()"
        }

        val returnStatement = StringBuilder(
            "return %T(data = $dataStatement",
        )

        if (includedListStatement != null) {
            returnStatement.append(", ")
            returnStatement.append("included = $includedListStatement")
        }

        returnStatement.append(", meta = rootMeta")
        returnStatement.append(", links = rootLinks")

        returnStatement.append(")")
        return FunSpec.builder(JsonApiConstants.Members.JSONX_WRAPPER_GETTER)
            .receiver(modelClass)
            .returns(wrapperClass)
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray(),
            )
            .build()
    }
}
