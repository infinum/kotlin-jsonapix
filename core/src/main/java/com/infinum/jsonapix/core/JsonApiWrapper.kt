package com.infinum.jsonapix.core

interface JsonApiWrapper<out T> {
    val id: Int
    val type: String
    val data: T
}