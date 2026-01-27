package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.JsonApiXListSpecBuilder
import com.infinum.jsonapix.processor.specs.specbuilders.JsonApiXSpecBuilder
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal class WrapperSpecsGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?,
    private val customError: ClassName?
) : SpecGenerator {

    override fun generate(outputDir: File) {
        // Generate JsonApiX wrapper
        val wrapperFileSpec = JsonApiXSpecBuilder.build(
            className = holder.className,
            isNullable = holder.isNullable,
            type = holder.type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            customError = customError
        )
        wrapperFileSpec.writeTo(outputDir)

        // Generate JsonApiXList wrapper
        val wrapperListFileSpec = JsonApiXListSpecBuilder.build(
            className = holder.className,
            isNullable = holder.isNullable,
            type = holder.type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            customError = customError
        )
        wrapperListFileSpec.writeTo(outputDir)
    }
}
