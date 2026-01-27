package com.infinum.jsonapix.processor.models

import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.squareup.kotlinpoet.ClassName

internal data class JsonApiXMetaHolder(
    val type: String,
    val placementStrategy: MetaPlacementStrategy,
    val className: ClassName
) : Holder
