package com.infinum.jsonapix.processor.extensions

import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.squareup.kotlinpoet.ClassName

internal fun Set<JsonApiXLinksHolder>.toLinksInfo(type: String): LinksInfo? {
    val holdersForType = filter { it.type == type }
    if (holdersForType.isEmpty()) return null

    return LinksInfo(type).apply {
        holdersForType.forEach { holder ->
            when (holder.placementStrategy) {
                LinksPlacementStrategy.ROOT -> rootLinks = holder.className
                LinksPlacementStrategy.DATA -> resourceObjectLinks = holder.className
                LinksPlacementStrategy.RELATIONSHIPS -> relationshipsLinks = holder.className
            }
        }
    }
}

internal fun Set<JsonApiXMetaHolder>.toMetaInfo(type: String): MetaInfo? {
    val holdersForType = filter { it.type == type }
    if (holdersForType.isEmpty()) return null

    return MetaInfo(type).apply {
        holdersForType.forEach { holder ->
            when (holder.placementStrategy) {
                MetaPlacementStrategy.ROOT -> rootClassName = holder.className
                MetaPlacementStrategy.DATA -> resourceObjectClassName = holder.className
                MetaPlacementStrategy.RELATIONSHIPS -> relationshipsClassNAme = holder.className
            }
        }
    }
}

internal fun Set<JsonApiXErrorHolder>.toCustomError(type: String): ClassName? {
    return find { it.type == type }?.className
}
