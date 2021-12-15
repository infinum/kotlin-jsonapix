package com.infinum.jsonapix.core.adapters

interface TypeAdapter<Model> {
    fun convertFromString(input: String): Model
    fun convertToString(input: Model): String
}
