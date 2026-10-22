package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.infinum.jsonapix.processor.specs.specbuilders.JsonApiModelSpecBuilder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

internal class ModelSpecGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?,
    private val customError: ClassName?,
) : SpecGenerator {

    override fun generate(): List<FileSpec> {
        val fileSpec = JsonApiModelSpecBuilder.build(
            className = holder.className,
            isRootNullable = holder.isNullable,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            customError = customError,
        )
        return listOf(fileSpec)
    }
}
