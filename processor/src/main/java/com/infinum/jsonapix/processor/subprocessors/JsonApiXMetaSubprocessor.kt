package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.collectors.JsonApiXMetaCollector
import com.infinum.jsonapix.processor.models.JsonApiXMetaResult
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXMetaSubprocessor : CommonSubprocessor<JsonApiXMetaResult>() {

    override fun process(roundEnvironment: RoundEnvironment): JsonApiXMetaResult {
        val collector = JsonApiXMetaCollector(roundEnvironment, elementUtils)
        val validator = JsonApiXMetaValidator()

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        if (validatedHolders.isEmpty()) {
            return JsonApiXMetaResult.EMPTY
        }

        val metaInfoMap = mutableMapOf<String, MetaInfo>()
        val customMetaClassNames = mutableListOf<ClassName>()

        validatedHolders.forEach { holder ->
            customMetaClassNames.add(holder.className)

            val metaInfo = metaInfoMap.getOrPut(holder.type) { MetaInfo(holder.type) }

            when (holder.placementStrategy) {
                MetaPlacementStrategy.ROOT -> metaInfo.rootClassName = holder.className
                MetaPlacementStrategy.DATA -> metaInfo.resourceObjectClassName = holder.className
                MetaPlacementStrategy.RELATIONSHIPS -> metaInfo.relationshipsClassNAme = holder.className
            }
        }

        return JsonApiXMetaResult(metaInfoMap, customMetaClassNames)
    }
}
