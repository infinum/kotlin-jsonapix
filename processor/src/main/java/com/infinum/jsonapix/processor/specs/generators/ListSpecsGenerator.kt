package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.model.JsonApiListItemSpecBuilder
import com.infinum.jsonapix.processor.specs.model.JsonApiListSpecBuilder
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal class ListSpecsGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?,
    private val customError: ClassName?
) : SpecGenerator {

    override fun generate(outputDir: File) {
        // Generate ListItem
        val listItemFileSpec = JsonApiListItemSpecBuilder.build(
            className = holder.className,
            isRootNullable = holder.isNullable,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            customError = customError
        )
        listItemFileSpec.writeTo(outputDir)

        // Generate List
        val listFileSpec = JsonApiListSpecBuilder.build(
            className = holder.className,
            isRootNullable = holder.isNullable,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            customError = customError
        )
        listFileSpec.writeTo(outputDir)
    }
}
