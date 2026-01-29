package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.infinum.jsonapix.processor.specs.specbuilders.TypeAdapterListSpecBuilder
import com.infinum.jsonapix.processor.specs.specbuilders.TypeAdapterSpecBuilder
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal class TypeAdapterSpecsGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?,
    private val customError: ClassName?,
) : SpecGenerator {

    override fun generate(outputDir: File) {
        // Generate TypeAdapter
        val typeAdapterFileSpec = TypeAdapterSpecBuilder.build(
            className = holder.className,
            linksInfo = linksInfo,
            metaInfo = metaInfo,
            errors = customError?.canonicalName,
        )
        typeAdapterFileSpec.writeTo(outputDir)

        // Generate TypeAdapterList
        val typeAdapterListFileSpec = TypeAdapterListSpecBuilder.build(
            className = holder.className,
            linksInfo = linksInfo,
            metaInfo = metaInfo,
            errors = customError?.canonicalName,
        )
        typeAdapterListFileSpec.writeTo(outputDir)
    }
}
