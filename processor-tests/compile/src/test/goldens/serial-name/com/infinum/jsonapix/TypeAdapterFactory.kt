package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.TypeAdapterList_Owner
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Pet
import com.infinum.jsonapix.`data`.models.TypeAdapter_Owner
import com.infinum.jsonapix.`data`.models.TypeAdapter_Pet
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type.qualifiedName) {
    "com.infinum.jsonapix.data.models.PetModel" -> TypeAdapter_Pet()
    "com.infinum.jsonapix.data.models.OwnerModel" -> TypeAdapter_Owner()
    "com.infinum.jsonapix.data.models.PetList" -> TypeAdapterList_Pet()
    "com.infinum.jsonapix.data.models.OwnerList" -> TypeAdapterList_Owner()
    else -> null
  }
}
