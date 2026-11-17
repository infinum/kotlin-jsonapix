package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.DogList
import com.infinum.jsonapix.`data`.models.DogModel
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Dog
import com.infinum.jsonapix.`data`.models.TypeAdapter_Dog
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    DogModel::class -> TypeAdapter_Dog()
    DogList::class -> TypeAdapterList_Dog()
    else -> null
  }
}
