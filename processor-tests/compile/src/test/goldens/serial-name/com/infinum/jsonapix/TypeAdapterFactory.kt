package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.OwnerList
import com.infinum.jsonapix.`data`.models.OwnerModel
import com.infinum.jsonapix.`data`.models.PetList
import com.infinum.jsonapix.`data`.models.PetModel
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Owner
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Pet
import com.infinum.jsonapix.`data`.models.TypeAdapter_Owner
import com.infinum.jsonapix.`data`.models.TypeAdapter_Pet
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    OwnerModel::class -> TypeAdapter_Owner()
    PetModel::class -> TypeAdapter_Pet()
    OwnerList::class -> TypeAdapterList_Owner()
    PetList::class -> TypeAdapterList_Pet()
    else -> null
  }
}
