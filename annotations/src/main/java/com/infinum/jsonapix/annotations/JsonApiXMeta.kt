package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JvmRepeatable(JsonApiXMetaList::class)
annotation class JsonApiXMeta(
    val type: String,
    val placementStrategy: MetaPlacementStrategy = MetaPlacementStrategy.ROOT,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApiXMetaList(val value: Array<JsonApiXMeta>)

enum class MetaPlacementStrategy {
    ROOT, DATA, RELATIONSHIPS
}
