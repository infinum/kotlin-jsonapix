package com.infinum.jsonapix.processor.specs

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import java.lang.StringBuilder

object IncludedModelSpecBuilder {

    fun build(
        oneRelationships: List<PropertySpec>,
        manyRelationships: List<PropertySpec>
    ): CodeBlock {
        val statement = StringBuilder("listOf(")
        oneRelationships.forEachIndexed { index, prop ->
            statement.append("${prop.name}.toResourceObject()")
            if (index != oneRelationships.lastIndex || (index == oneRelationships.lastIndex && manyRelationships.isNotEmpty())) {
                statement.append(", ")
            }
        }

        manyRelationships.forEachIndexed { index, prop ->
            statement.append("*${prop.name}.map { it.toResourceObject() }.toTypedArray()")
            if (index != manyRelationships.lastIndex) {
                statement.append(", ")
            }
        }
        statement.append(")")

        return CodeBlock.of(statement.toString())
    }
}