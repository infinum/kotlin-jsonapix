package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import java.lang.StringBuilder

internal object IncludedSpecBuilder {

    fun build(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>,
    ): CodeBlock {
        val statement = StringBuilder("listOfNotNull(")

        oneRelationships.forEachIndexed { index, prop ->

            statement.append(
                """data.${prop.name}?.let{it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
                    relationshipsMeta?.get("${prop.name}"),
                    relationshipsLinks?.get("${prop.name}")
                )}
                """.trimMargin(),
            )
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                statement.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, prop ->
            statement.append(
                """*data.${prop.name}.mapSafe { it.let{it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
                        relationshipsMeta?.get("${prop.name}"),
                        relationshipsLinks?.get("${prop.name}")
                    )}}.toTypedArray()
                """.trimMargin(),
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
        manyRelationships: List<PropertySpec>,
    ): CodeBlock {
        val statement = StringBuilder("listOfNotNull(")
        oneRelationships.forEachIndexed { index, prop ->
            statement.append(
                """*data.mapSafe {item ->
                    item.data.${prop.name}?.let{it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
                        item.relationshipsMeta?.get("${prop.name}"),
                        item.relationshipsLinks?.get("${prop.name}")
                    )} }.toTypedArray()
                """.trimMargin(),
            )
            if (index != oneRelationships.lastIndex ||
                (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())
            ) {
                statement.append(",")
            }
        }

        manyRelationships.forEachIndexed { index, prop ->
            statement.append("*data.flatMapSafe {item-> item.data.${prop.name}.mapSafe {\n")
            statement.append(
                """it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
                     item.relationshipsMeta?.get("${prop.name}"),
                     item.relationshipsLinks?.get("${prop.name}")
                )
                """.trimMargin(),
            )
            statement.append("} }.toTypedArray()")
            if (index != manyRelationships.lastIndex) {
                statement.append(", ")
            }
        }
        statement.append(")")

        return CodeBlock.of(statement.toString())
    }
}
