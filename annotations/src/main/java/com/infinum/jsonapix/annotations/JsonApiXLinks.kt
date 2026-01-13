package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JvmRepeatable(JsonApiXLinksList::class)
annotation class JsonApiXLinks(
    val type: String,
    val placementStrategy: LinksPlacementStrategy = LinksPlacementStrategy.ROOT,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApiXLinksList(
    val value: Array<JsonApiXLinks>,
)

enum class LinksPlacementStrategy {
    ROOT,
    DATA,
    RELATIONSHIPS,
}
