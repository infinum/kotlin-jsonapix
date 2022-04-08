package com.infinum.jsonapix.core.adapters

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

interface AdapterFactory {
    fun getAdapter(type: KClass<*>): TypeAdapter<*>?

    fun getAdapter(type: ParameterizedType): TypeAdapter<*>?
}
