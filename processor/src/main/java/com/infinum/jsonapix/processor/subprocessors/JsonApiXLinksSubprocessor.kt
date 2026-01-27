package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.collectors.JsonApiXLinksCollector
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXLinksSubprocessor : CommonSubprocessor() {

    // TODO check if there is a better way to expose this info
    private val _linksInfoMap = mutableMapOf<String, LinksInfo>()
    private val _customLinksClassNames = mutableListOf<ClassName>()

    val linksInfoMap: Map<String, LinksInfo>
        get() = _linksInfoMap

    val customLinksClassNames: List<ClassName>
        get() = _customLinksClassNames

    override fun process(roundEnvironment: RoundEnvironment) {
        val collector = JsonApiXLinksCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXLinksValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        validatedHolders.forEach { holder ->
            processLinksHolder(holder)
        }
    }

    private fun processLinksHolder(holder: JsonApiXLinksHolder) {
        _customLinksClassNames.add(holder.className)

        val linksInfo = _linksInfoMap.getOrPut(holder.type) { LinksInfo(holder.type) }

        when (holder.placementStrategy) {
            LinksPlacementStrategy.ROOT -> linksInfo.rootLinks = holder.className
            LinksPlacementStrategy.DATA -> linksInfo.resourceObjectLinks = holder.className
            LinksPlacementStrategy.RELATIONSHIPS -> linksInfo.relationshipsLinks = holder.className
        }
    }
}
