package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.processor.KaptCodeOutput
import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXErrorCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.generation.JsonApiXGenerator
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import com.infinum.jsonapix.processor.validators.JsonApiXValidator
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXSubprocessor : CommonSubprocessor() {

    override fun process(roundEnvironment: RoundEnvironment) {
        val holders = collectJsonApiX(roundEnvironment)
        if (holders.isEmpty()) return

        val linksHolders = collectLinks(roundEnvironment)
        val metaHolders = collectMeta(roundEnvironment)
        val errorHolders = collectErrors(roundEnvironment)

        JsonApiXGenerator(KaptCodeOutput(filer, elementUtils)).generate(
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
}
