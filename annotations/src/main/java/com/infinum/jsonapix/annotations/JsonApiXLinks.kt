package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class JsonApiXLinks(
    val type: String,
    val placementStrategy: LinksPlacementStrategy = LinksPlacementStrategy.ROOT,
)

enum class LinksPlacementStrategy {
    ROOT, DATA, RELATIONSHIPS
}
