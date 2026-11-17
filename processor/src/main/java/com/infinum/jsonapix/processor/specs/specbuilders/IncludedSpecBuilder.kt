package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.extensions.isRelationshipWrapper
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
                """data.${prop.name}${prop.relatedResource()}?.let{it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
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
                    item.data.${prop.name}${prop.relatedResource()}?.let{it.${JsonApiConstants.Members.TO_RESOURCE_OBJECT}(
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

    /**
     * How to reach the related model from the property.
     *
     * Only a `Relationship.Included` carries one, so a `Relationship<T>` property goes through the
     * `value` accessor and the other three states contribute nothing to `included`. A `Linked`
     * relationship points at a resource the caller never materialized, which is exactly a
     * relationship whose resource does not belong in `included`.
     */
    private fun PropertySpec.relatedResource(): String = when {
        !isRelationshipWrapper -> ""
        type.isNullable -> "?.${JsonApiConstants.Imports.RELATIONSHIP_VALUE}"
        else -> ".${JsonApiConstants.Imports.RELATIONSHIP_VALUE}"
    }
}
