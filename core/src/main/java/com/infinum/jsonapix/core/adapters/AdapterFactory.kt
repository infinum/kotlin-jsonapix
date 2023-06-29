package com.infinum.jsonapix.core.adapters

import kotlin.reflect.KClass

interface AdapterFactory {
    fun getAdapter(type: KClass<*>): TypeAdapter<*>?
}

inline fun <reified T> AdapterFactory.getAdapter(): TypeAdapter<T>? = getAdapter(T::class) as? TypeAdapter<T>
