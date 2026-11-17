package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.DogList
import com.infinum.jsonapix.`data`.models.DogModel
import com.infinum.jsonapix.`data`.models.PersonList
import com.infinum.jsonapix.`data`.models.PersonModel
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Dog
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Person
import com.infinum.jsonapix.`data`.models.TypeAdapter_Dog
import com.infinum.jsonapix.`data`.models.TypeAdapter_Person
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    DogModel::class -> TypeAdapter_Dog()
    PersonModel::class -> TypeAdapter_Person()
    DogList::class -> TypeAdapterList_Dog()
    PersonList::class -> TypeAdapterList_Person()
    else -> null
  }
}
