package com.infinum.jsonapix.processor

interface JsonApiWrapper<out T> {
    val id: Int
    val type: String
    val data: T
}