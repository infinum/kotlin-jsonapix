package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

internal abstract class BaseResourceObjectFunSpecBuilder() {

    abstract fun getClassSuffix(): String
    fun build(
        originalClass: ClassName,
        resourceObjectClass: ClassName,
        attributesClass: ClassName?,
        relationshipsClass: ClassName?
    ): FunSpec {
        val modelClassName = ClassName.bestGuess(originalClass.canonicalName.withName(getClassSuffix()))

        val returnStatement = StringBuilder("return %T(")
        val builderArgs = mutableListOf<Any>(resourceObjectClass)

        returnStatement.append("id = id ?: \"0\", ")

        if (attributesClass != null) {
            returnStatement.append(
                "attributes = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(data), "
            )
            builderArgs.add(attributesClass)
        }

        if (relationshipsClass != null) {
            returnStatement.append(
                "relationships = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(data), "
            )
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append("meta = resourceObjectMeta, ")
        returnStatement.append("links = resourceObjectLinks")

        returnStatement.append(")")

        return FunSpec.builder(JsonApiConstants.Members.TO_RESOURCE_OBJECT)
            .receiver(modelClassName)
            .returns(resourceObjectClass)
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray()
            )
            .build()
    }
}
