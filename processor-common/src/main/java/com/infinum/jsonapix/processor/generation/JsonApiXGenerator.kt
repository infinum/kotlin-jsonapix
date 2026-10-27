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

public class JsonApiXGenerator(
    private val output: CodeOutput,
) {

    public fun generate(
        holders: Set<JsonApiXHolder>,
        linksHolders: Set<JsonApiXLinksHolder>,
        metaHolders: Set<JsonApiXMetaHolder>,
        errorHolders: Set<JsonApiXErrorHolder>,
    ) {
        // Backends find annotated classes in the order of the file system's directory listings, which
        // differs between machines and between backends. Sorted by class name, the generated files are
        // the same everywhere. The sort is stable, so a class's repeated annotations keep their order.
        val sortedHolders = holders.sortedBy { it.className }.toSet()
        val sortedLinksHolders = linksHolders.sortedBy { it.className }.toSet()
        val sortedMetaHolders = metaHolders.sortedBy { it.className }.toSet()
        val sortedErrorHolders = errorHolders.sortedBy { it.className }.toSet()

        // Generate per-holder specs, each originating from its own model
        sortedHolders.forEach { holder ->
            val metaInfo = sortedMetaHolders.toMetaInfo(holder.type)
            val linksInfo = sortedLinksHolders.toLinksInfo(holder.type)
            val customError = sortedErrorHolders.toCustomError(holder.type)

            listOf(
                AttributesSpecGenerator(holder),
                RelationshipsSpecGenerator(holder),
                ResourceObjectSpecGenerator(holder, metaInfo, linksInfo),
                WrapperSpecsGenerator(holder, metaInfo, linksInfo, customError),
                ModelSpecGenerator(holder, metaInfo, linksInfo, customError),
                ListSpecsGenerator(holder, metaInfo, linksInfo, customError),
                TypeAdapterSpecsGenerator(holder, metaInfo, linksInfo, customError),
            ).write(originatingModels = setOf(holder.className), aggregating = false)
        }

        // Generate aggregate specs, each originating from every model
        listOf(
            JsonXExtensionsSpecGenerator(sortedHolders, sortedLinksHolders, sortedMetaHolders, sortedErrorHolders),
            TypeAdapterFactorySpecGenerator(sortedHolders),
        ).write(originatingModels = sortedHolders.mapTo(linkedSetOf()) { it.className }, aggregating = true)
    }

    private fun List<SpecGenerator>.write(originatingModels: Set<ClassName>, aggregating: Boolean) {
        forEach { generator ->
            generator.generate().forEach { fileSpec -> output.write(fileSpec, originatingModels, aggregating) }
        }
    }
}
