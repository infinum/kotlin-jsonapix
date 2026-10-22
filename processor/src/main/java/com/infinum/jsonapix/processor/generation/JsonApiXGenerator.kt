package com.infinum.jsonapix.processor.generation

import com.infinum.jsonapix.processor.extensions.toCustomError
import com.infinum.jsonapix.processor.extensions.toLinksInfo
import com.infinum.jsonapix.processor.extensions.toMetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.specs.generators.AttributesSpecGenerator
import com.infinum.jsonapix.processor.specs.generators.JsonXExtensionsSpecGenerator
import com.infinum.jsonapix.processor.specs.generators.ListSpecsGenerator
import com.infinum.jsonapix.processor.specs.generators.ModelSpecGenerator
import com.infinum.jsonapix.processor.specs.generators.RelationshipsSpecGenerator
import com.infinum.jsonapix.processor.specs.generators.ResourceObjectSpecGenerator
import com.infinum.jsonapix.processor.specs.generators.SpecGenerator
import com.infinum.jsonapix.processor.specs.generators.TypeAdapterFactorySpecGenerator
import com.infinum.jsonapix.processor.specs.generators.TypeAdapterSpecsGenerator
import com.infinum.jsonapix.processor.specs.generators.WrapperSpecsGenerator
import com.squareup.kotlinpoet.ClassName

internal class JsonApiXGenerator(
    private val output: CodeOutput,
) {

    fun generate(
        holders: Set<JsonApiXHolder>,
        linksHolders: Set<JsonApiXLinksHolder>,
        metaHolders: Set<JsonApiXMetaHolder>,
        errorHolders: Set<JsonApiXErrorHolder>,
    ) {
        // Generate per-holder specs, each originating from its own model
        holders.forEach { holder ->
            val metaInfo = metaHolders.toMetaInfo(holder.type)
            val linksInfo = linksHolders.toLinksInfo(holder.type)
            val customError = errorHolders.toCustomError(holder.type)

            listOf(
                AttributesSpecGenerator(holder),
                RelationshipsSpecGenerator(holder),
                ResourceObjectSpecGenerator(holder, metaInfo, linksInfo),
                WrapperSpecsGenerator(holder, metaInfo, linksInfo, customError),
                ModelSpecGenerator(holder, metaInfo, linksInfo, customError),
                ListSpecsGenerator(holder, metaInfo, linksInfo, customError),
                TypeAdapterSpecsGenerator(holder, metaInfo, linksInfo, customError),
            ).write(originatingModels = setOf(holder.className))
        }

        // Generate aggregate specs, each originating from every model
        listOf(
            JsonXExtensionsSpecGenerator(holders, linksHolders, metaHolders, errorHolders),
            TypeAdapterFactorySpecGenerator(holders),
        ).write(originatingModels = holders.mapTo(linkedSetOf()) { it.className })
    }

    private fun List<SpecGenerator>.write(originatingModels: Set<ClassName>) {
        forEach { generator ->
            generator.generate().forEach { fileSpec -> output.write(fileSpec, originatingModels) }
        }
    }
}
