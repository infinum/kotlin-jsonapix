package com.infinum.jsonapix.core.adapters

import kotlin.reflect.KClass

interface AdapterFactory {
    fun getAdapter(type: KClass<*>): TypeAdapter<*>?
}
