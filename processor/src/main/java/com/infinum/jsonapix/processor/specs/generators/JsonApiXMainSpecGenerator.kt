package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXErrorResult
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksResult
import com.infinum.jsonapix.processor.models.JsonApiXMetaResult
import java.io.File

internal class JsonApiXMainSpecGenerator(
    private val outputDir: File,
    private val holders: Set<JsonApiXHolder>,
    private val linksResult: JsonApiXLinksResult,
    private val metaResult: JsonApiXMetaResult,
    private val errorResult: JsonApiXErrorResult
) {

    fun generate() {
        // Generate per-holder specs
        holders.forEach { holder ->
            generateForHolder(holder)
        }

        // Generate aggregate specs
        JsonXExtensionsSpecGenerator(holders, linksResult, metaResult, errorResult).generate(outputDir)
        TypeAdapterFactorySpecGenerator(holders).generate(outputDir)
    }

    private fun generateForHolder(holder: JsonApiXHolder) {
        val metaInfo = metaResult.metaInfoMap[holder.type]
        val linksInfo = linksResult.linksInfoMap[holder.type]
        val customError = errorResult.customErrors[holder.type]

        AttributesSpecGenerator(holder).generate(outputDir)
        RelationshipsSpecGenerator(holder).generate(outputDir)
        ResourceObjectSpecGenerator(holder, metaInfo, linksInfo).generate(outputDir)
        WrapperSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        ModelSpecGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        ListSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
        TypeAdapterSpecsGenerator(holder, metaInfo, linksInfo, customError).generate(outputDir)
    }
}
