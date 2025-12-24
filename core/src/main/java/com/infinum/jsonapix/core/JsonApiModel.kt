package com.infinum.jsonapix.core

@Suppress("UnnecessaryAbstractClass")
abstract class JsonApiModel {
    private var type: String? = null
    private var id: String? = null

    fun setType(type: String?) {
        this.type = type
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun type(): String? = type

    @Suppress("FunctionMinLength")
    fun id(): String? = id
}
