package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.RelationshipsSpecBuilder
import com.squareup.kotlinpoet.FileSpec
import java.io.File

@Suppress("SpreadOperator")
internal class RelationshipsSpecGenerator(
    private val holder: JsonApiXHolder,
) : SpecGenerator {

    override fun generate(outputDir: File) {
        if (holder.oneRelationships.isEmpty() && holder.manyRelationships.isEmpty()) return

        val typeSpec = RelationshipsSpecBuilder.build(
            className = holder.className,
            type = holder.type,
            oneRelationships = holder.oneRelationships,
            manyRelationships = holder.manyRelationships,
        )

        val fileSpec = FileSpec.builder(holder.className.packageName, typeSpec.name!!)
            .addType(typeSpec)
            .addImport(JsonApiConstants.Packages.CORE, JsonApiConstants.Imports.JSON_API_MODEL)
            .addImport(JsonApiConstants.Packages.JSONX, *JsonApiConstants.Imports.RELATIONSHIP_EXTENSIONS)
            .build()

        fileSpec.writeTo(outputDir)
    }
}
