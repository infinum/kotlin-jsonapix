package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.extensions.toCustomError
import com.infinum.jsonapix.processor.extensions.toLinksInfo
import com.infinum.jsonapix.processor.extensions.toMetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import java.io.File

internal class JsonApiXMainSpecGenerator(
    private val outputDir: File,
    private val holders: Set<JsonApiXHolder>,
    private val linksHolders: Set<JsonApiXLinksHolder>,
    private val metaHolders: Set<JsonApiXMetaHolder>,
    private val errorHolders: Set<JsonApiXErrorHolder>
) {

    fun generate() {
        // Generate per-holder specs
        holders.forEach { holder ->
            generateForHolder(holder)
        }

        // Generate aggregate specs
        JsonXExtensionsSpecGenerator(holders, linksHolders, metaHolders, errorHolders).generate(outputDir)
        TypeAdapterFactorySpecGenerator(holders).generate(outputDir)
    }

    private fun generateForHolder(holder: JsonApiXHolder) {
        val metaInfo = metaHolders.toMetaInfo(holder.type)
        val linksInfo = linksHolders.toLinksInfo(holder.type)
        val customError = errorHolders.toCustomError(holder.type)

        AttributesSpecGenerator(holder).generate(outputDir)
        RelationshipsSpecGenerator(holder).generate(outputDir)
        ResourceObjectSpecGenerator(holder, metaInfo, linksInfo).generate(outputDir)
        WrapperSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        ModelSpecGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        ListSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        TypeAdapterSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
    }
}
