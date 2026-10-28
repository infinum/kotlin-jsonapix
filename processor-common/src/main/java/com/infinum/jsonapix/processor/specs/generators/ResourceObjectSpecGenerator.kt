package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.infinum.jsonapix.processor.specs.specbuilders.ResourceObjectSpecBuilder
import com.squareup.kotlinpoet.FileSpec

@Suppress("SpreadOperator")
internal class ResourceObjectSpecGenerator(
    private val holder: JsonApiXHolder,
    private val metaInfo: MetaInfo?,
    private val linksInfo: LinksInfo?,
) : SpecGenerator {

    override fun generate(): List<FileSpec> {
        val fileSpec = ResourceObjectSpecBuilder.build(
            className = holder.className,
            metaClassName = metaInfo?.resourceObjectClassName,
            linksInfo = linksInfo,
            type = holder.type,
            attributes = holder.attributes.map { it.property },
            oneRelationships = mapOf(*holder.oneRelationships.map { it.property.name to it.property.type }.toTypedArray()),
            manyRelationships = mapOf(*holder.manyRelationships.map { it.property.name to it.property.type }.toTypedArray()),
        )

        return listOf(fileSpec)
    }
}
