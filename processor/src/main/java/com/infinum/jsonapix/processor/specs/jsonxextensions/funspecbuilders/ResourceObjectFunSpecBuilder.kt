package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

internal object ResourceObjectFunSpecBuilder {

    fun build(
        originalClass: ClassName,
        resourceObjectClass: ClassName,
        attributesClass: ClassName?,
        relationshipsClass: ClassName?
    ): FunSpec {
        val returnStatement = StringBuilder("return %T(")
        val builderArgs = mutableListOf<Any>(resourceObjectClass)

        returnStatement.append("id = (this as? ")
            .append("JsonApiModel")
            .append(")?.let { this.id() } ?: \"0\", ")

        if (attributesClass != null) {
            returnStatement.append(
                "attributes = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(this)"
            )
            builderArgs.add(attributesClass)
        }

        if (relationshipsClass != null) {
            if (attributesClass != null) {
                returnStatement.append(", ")
            }
            returnStatement.append(
                "relationships = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(this)"
            )
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append(")")

        return FunSpec.builder(JsonApiConstants.Members.TO_RESOURCE_OBJECT)
            .receiver(originalClass)
            .returns(resourceObjectClass)
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray()
            )
            .build()
    }
}
