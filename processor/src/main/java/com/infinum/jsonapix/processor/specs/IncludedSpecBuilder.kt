package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import java.lang.StringBuilder

internal object IncludedSpecBuilder {

    fun build(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): CodeBlock {
        val statement = StringBuilder("listOfNotNull(")

        oneRelationships.forEachIndexed { index, prop ->

            statement.append("data.${prop.name}?.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}()")
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                statement.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, prop ->
            statement.append(
                "*data.${prop.name}.mapSafe { it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }.toTypedArray()"
            )
            if (index != manyRelationships.lastIndex) {
                statement.append(", ")
            }
        }
        statement.append(")")

        return CodeBlock.of(statement.toString())
    }

    fun buildForList(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): CodeBlock {
        val statement = StringBuilder("listOfNotNull(")
        oneRelationships.forEachIndexed { index, prop ->
            statement.append(
                "*data.mapSafe { it.data.${prop.name}?.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}() }.toTypedArray()"
            )
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                statement.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, prop ->
            statement.append("*data.flatMapSafe { it.data.${prop.name}.mapSafe { ")
            statement.append("it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}()")
            statement.append("} }.toTypedArray()")
            if (index != manyRelationships.lastIndex) {
                statement.append(", ")
            }
        }
        statement.append(")")

        return CodeBlock.of(statement.toString())
    }
}
