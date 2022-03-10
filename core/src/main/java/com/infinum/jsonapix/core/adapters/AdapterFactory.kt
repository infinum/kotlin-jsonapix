package com.infinum.jsonapix.core.adapters

interface AdapterFactory {
    fun getAdapter(type: String): TypeAdapter<*>?
}
