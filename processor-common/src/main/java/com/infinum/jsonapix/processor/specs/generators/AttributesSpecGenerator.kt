package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.AttributesSpecBuilder
import com.squareup.kotlinpoet.FileSpec

internal class AttributesSpecGenerator(
    private val holder: JsonApiXHolder,
) : SpecGenerator {

    override fun generate(): List<FileSpec> {
        if (holder.attributes.isEmpty()) return emptyList()

        val typeSpec = AttributesSpecBuilder.build(
            className = holder.className,
            attributes = holder.attributes,
            type = holder.type,
        )

        val fileSpec = FileSpec.builder(holder.className.packageName, requireNotNull(typeSpec.name))
            .addType(typeSpec)
            .build()

        return listOf(fileSpec)
    }
}
