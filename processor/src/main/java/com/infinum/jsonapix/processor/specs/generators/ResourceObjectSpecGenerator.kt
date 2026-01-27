package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.ResourceObjectSpecBuilder
import java.io.File

internal class ResourceObjectSpecGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?
) : SpecGenerator {

    override fun generate(outputDir: File) {
        val fileSpec = ResourceObjectSpecBuilder.build(
            className = holder.className,
            metaClassName = metaInfo?.resourceObjectClassName,
            linksInfo = linksInfo,
            type = holder.type,
            attributes = holder.primitiveProperties,
            oneRelationships = mapOf(*holder.oneRelationships.map { it.name to it.type }.toTypedArray()),
            manyRelationships = mapOf(*holder.manyRelationships.map { it.name to it.type }.toTypedArray())
        )

        fileSpec.writeTo(outputDir)
    }
}
