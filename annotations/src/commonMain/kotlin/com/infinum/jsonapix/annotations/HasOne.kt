package com.infinum.jsonapix.annotations

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasOne(val type: String)
