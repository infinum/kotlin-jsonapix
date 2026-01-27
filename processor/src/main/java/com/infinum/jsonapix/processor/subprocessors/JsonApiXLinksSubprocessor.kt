package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.models.JsonApiXLinksResult
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXLinksSubprocessor : CommonSubprocessor<JsonApiXLinksResult>() {

    override fun process(roundEnvironment: RoundEnvironment): JsonApiXLinksResult {
        val collector = JsonApiXLinksCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXLinksValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        if (validatedHolders.isEmpty()) {
            return JsonApiXLinksResult.EMPTY
        }

        val linksInfoMap = mutableMapOf<String, LinksInfo>()
        val customLinksClassNames = mutableListOf<ClassName>()

        validatedHolders.forEach { holder ->
            customLinksClassNames.add(holder.className)

            val linksInfo = linksInfoMap.getOrPut(holder.type) { LinksInfo(holder.type) }

            when (holder.placementStrategy) {
                LinksPlacementStrategy.ROOT -> linksInfo.rootLinks = holder.className
                LinksPlacementStrategy.DATA -> linksInfo.resourceObjectLinks = holder.className
                LinksPlacementStrategy.RELATIONSHIPS -> linksInfo.relationshipsLinks = holder.className
            }
        }

        return JsonApiXLinksResult(linksInfoMap, customLinksClassNames)
    }
}
