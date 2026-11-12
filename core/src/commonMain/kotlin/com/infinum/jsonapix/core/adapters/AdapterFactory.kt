package com.infinum.jsonapix.core.adapters

import kotlin.reflect.KClass

interface AdapterFactory {
    fun getAdapter(type: KClass<*>): TypeAdapter<*>?
}

/**
 * Extension function to retrieve a TypeAdapter for a specific type [T] from an AdapterFactory.
 *
 * This function uses unchecked casting (as? TypeAdapter<T>) to obtain a TypeAdapter for the provided type.
 * The safety of this casting is ensured by the implementation of the AdapterFactory, which is responsible for
 * generating TypeAdapters for various types and registering them correctly.
 *
 * The generated code for the AdapterFactory ensures that it can produce TypeAdapters for all possible types.
 * It uses reflection to map KClass objects to their corresponding TypeAdapters.
 *
 * For example, the generated TypeAdapterFactory class contains a mapping for various qualified class names to
 * their respective TypeAdapters. When a specific type is requested using this extension function, it retrieves
 * the corresponding TypeAdapter from this mapping and returns it.
 *
 * @return The TypeAdapter for the requested type [T], or null if it doesn't exist.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> AdapterFactory.getAdapter(): TypeAdapter<T>? = getAdapter(T::class) as? TypeAdapter<T>
