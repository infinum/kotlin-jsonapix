package com.infinum.jsonapix.core.adapters

import kotlin.reflect.KClass

interface AdapterFactory {
    fun getAdapter(type: KClass<*>): TypeAdapter<*>?

    fun getListAdapter(type: KClass<*>): TypeAdapter<*>?
}

inline fun <reified T> AdapterFactory.getAdapter(): TypeAdapter<T>? = getAdapter(T::class) as? TypeAdapter<T>

inline fun <reified T> AdapterFactory.getListAdapter(): TypeAdapter<T>? = getListAdapter(T::class) as? TypeAdapter<T>
