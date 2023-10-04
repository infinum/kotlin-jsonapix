package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asClassName

internal object OriginalDataResourceObjectFunSpecBuilder {
    fun build(
        originalClass: ClassName,
        resourceObjectClass: ClassName,
        attributesClass: ClassName?,
        relationshipsClass: ClassName?,
        resourceMeta: ClassName?,
    ): FunSpec {

        val returnStatement = StringBuilder("return %T(")
        val builderArgs = mutableListOf<Any>(resourceObjectClass)

        returnStatement.append("id = id() ?: \"0\", ")

        if (attributesClass != null) {
            returnStatement.append(
                "attributes = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(this), "
            )
            builderArgs.add(attributesClass)
        }

        if (relationshipsClass != null) {
            returnStatement.append(
                "relationships = %T.${JsonApiConstants.Members.FROM_ORIGINAL_OBJECT}(this), "
            )
            builderArgs.add(relationshipsClass)
        }

        returnStatement.append("meta = meta as? %T, ")
        builderArgs.add(
            resourceMeta ?: Meta::class
        )
        returnStatement.append("links = links")

        returnStatement.append(")")

        return FunSpec.builder(JsonApiConstants.Members.TO_RESOURCE_OBJECT)
            .receiver(originalClass)
            .returns(resourceObjectClass)
            .addParameter(
                "meta",
                Meta::class.asClassName().copy(nullable = true),
            )
            .addParameter(
                "links",
                Links::class.asClassName().copy(nullable = true)
            )
            .addStatement(
                format = returnStatement.toString(),
                args = builderArgs.toTypedArray()
            )
            .build()
    }
}
