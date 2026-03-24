package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
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
import com.infinum.jsonapix.processor.specs.generators.TypeAdapterFactorySpecGenerator
import com.infinum.jsonapix.processor.specs.generators.TypeAdapterSpecsGenerator
import com.infinum.jsonapix.processor.specs.generators.WrapperSpecsGenerator
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import com.infinum.jsonapix.processor.validators.JsonApiXValidator
import java.io.File
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXSubprocessor : CommonSubprocessor() {

    override fun process(roundEnvironment: RoundEnvironment) {
        val holders = collectJsonApiX(roundEnvironment)
        if (holders.isEmpty()) return

        val linksHolders = collectLinks(roundEnvironment)
        val metaHolders = collectMeta(roundEnvironment)
        val errorHolders = collectErrors(roundEnvironment)

        val dir = outputDir ?: return

        generate(
            outputDir = dir,
            holders = holders,
            linksHolders = linksHolders,
            metaHolders = metaHolders,
            errorHolders = errorHolders,
        )
    }

    private fun collectJsonApiX(roundEnvironment: RoundEnvironment): Set<JsonApiXHolder> {
        val holders = JsonApiXCollector(roundEnvironment, elementUtils, typeUtils).collect()
        val validator = JsonApiXValidator()
        return validator.validate(holders)
    }

    private fun collectLinks(roundEnvironment: RoundEnvironment): Set<JsonApiXLinksHolder> {
        val holders = JsonApiXLinksCollector(roundEnvironment, elementUtils).collect()
        val validator = JsonApiXLinksValidator()
        return validator.validate(holders)
    }

    private fun collectMeta(roundEnvironment: RoundEnvironment): Set<JsonApiXMetaHolder> {
        val holders = JsonApiXMetaCollector(roundEnvironment, elementUtils).collect()
        val validator = JsonApiXMetaValidator()
        return validator.validate(holders)
    }

    private fun collectErrors(roundEnvironment: RoundEnvironment): Set<JsonApiXErrorHolder> {
        val holders = JsonApiXErrorCollector(roundEnvironment, elementUtils).collect()
        val validator = JsonApiXErrorValidator()
        return validator.validate(holders)
    }

    private fun generate(
        outputDir: File,
        holders: Set<JsonApiXHolder>,
        linksHolders: Set<JsonApiXLinksHolder>,
        metaHolders: Set<JsonApiXMetaHolder>,
        errorHolders: Set<JsonApiXErrorHolder>,
    ) {
        // Generate per-holder specs
        holders.forEach { holder ->
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

        // Generate aggregate specs
        JsonXExtensionsSpecGenerator(holders, linksHolders, metaHolders, errorHolders).generate(outputDir)
        TypeAdapterFactorySpecGenerator(holders).generate(outputDir)
    }
}
