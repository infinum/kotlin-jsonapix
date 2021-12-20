package com.infinum.jsonapix.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Links(val parentType: KClass<Any>, val placementStrategy: LinksPlacementStrategy = LinksPlacementStrategy.ROOT)

enum class LinksPlacementStrategy {
    ROOT, RESOURCE_OBJECT, RELATIONSHIPS, INCLUDED
}
