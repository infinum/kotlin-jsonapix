package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasMany(val type: String)
