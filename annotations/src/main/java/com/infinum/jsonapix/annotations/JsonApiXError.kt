package com.infinum.jsonapix.annotations

/**
 * Use with classes to generate JSONAPI serializable version of the class
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JvmRepeatable(JsonApiXErrorList::class)
annotation class JsonApiXError(
    val type: String,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApiXErrorList(
    val value: Array<JsonApiXError>,
)
