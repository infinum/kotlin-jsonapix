package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class JsonApiXMeta(
    val type: String,
    val placementStrategy: MetaPlacementStrategy = MetaPlacementStrategy.ROOT,
)

enum class MetaPlacementStrategy {
    ROOT, DATA, RELATIONSHIPS
}
