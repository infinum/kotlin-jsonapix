package com.infinum.jsonapix.core.resources

interface ResourceObject<out T> {
    val id: String
    val type: String
    val attributes: T?
}
