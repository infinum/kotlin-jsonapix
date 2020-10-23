package com.infinum.jsonapix.annotations

/**
 * Use with classes to generate JSONAPI serializable version of the class
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApiSerializable(val type: String)