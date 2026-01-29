package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.AttributesSpecBuilder
import com.squareup.kotlinpoet.FileSpec
import java.io.File

internal class AttributesSpecGenerator(
    private val holder: JsonApiXHolder,
) : SpecGenerator {

    override fun generate(outputDir: File) {
        if (holder.primitiveProperties.isEmpty()) return

        val typeSpec = AttributesSpecBuilder.build(
            className = holder.className,
            attributes = holder.primitiveProperties,
            type = holder.type,
        )

        val fileSpec = FileSpec.builder(holder.className.packageName, typeSpec.name!!)
            .addType(typeSpec)
            .build()

        fileSpec.writeTo(outputDir)
    }
}
