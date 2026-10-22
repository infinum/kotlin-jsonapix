package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.TypeAdapterList_Address
import com.infinum.jsonapix.`data`.models.TypeAdapter_Address
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type.qualifiedName) {
    "com.infinum.jsonapix.data.models.AddressModel" -> TypeAdapter_Address()
    "com.infinum.jsonapix.data.models.AddressList" -> TypeAdapterList_Address()
    else -> null
  }
}
