package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXMetaSubprocessor : CommonSubprocessor() {

    private val _metaInfoMap = mutableMapOf<String, MetaInfo>()
    private val _customMetaClassNames = mutableListOf<ClassName>()

    val metaInfoMap: Map<String, MetaInfo>
        get() = _metaInfoMap

    val customMetaClassNames: List<ClassName>
        get() = _customMetaClassNames

    override fun process(roundEnvironment: RoundEnvironment) {
        val collector = JsonApiXMetaCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXMetaValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        validatedHolders.forEach { holder ->
            processMetaHolder(holder)
        }
    }

    private fun processMetaHolder(holder: JsonApiXMetaHolder) {
        _customMetaClassNames.add(holder.className)

        val metaInfo = _metaInfoMap.getOrPut(holder.type) { MetaInfo(holder.type) }

        when (holder.placementStrategy) {
            MetaPlacementStrategy.ROOT -> metaInfo.rootClassName = holder.className
            MetaPlacementStrategy.DATA -> metaInfo.resourceObjectClassName = holder.className
            MetaPlacementStrategy.RELATIONSHIPS -> metaInfo.relationshipsClassNAme = holder.className
        }
    }
}
