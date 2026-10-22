package com.infinum.jsonapix.processor.models

import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.squareup.kotlinpoet.ClassName

public data class JsonApiXLinksHolder(
    val type: String,
    val placementStrategy: LinksPlacementStrategy,
    val className: ClassName,
) : Holder
