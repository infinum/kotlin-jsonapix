package com.infinum.jsonapix.annotations

/**
 * Use with classes to generate JSONAPI serializable version of the class
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApiX(val type: String,val isNullable: Boolean = false)
