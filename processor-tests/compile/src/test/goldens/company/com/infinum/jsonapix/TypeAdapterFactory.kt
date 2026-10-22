package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.TypeAdapterList_Address
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Company
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Dog
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Person
import com.infinum.jsonapix.`data`.models.TypeAdapter_Address
import com.infinum.jsonapix.`data`.models.TypeAdapter_Company
import com.infinum.jsonapix.`data`.models.TypeAdapter_Dog
import com.infinum.jsonapix.`data`.models.TypeAdapter_Person
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type.qualifiedName) {
    "com.infinum.jsonapix.data.models.AddressModel" -> TypeAdapter_Address()
    "com.infinum.jsonapix.data.models.PersonModel" -> TypeAdapter_Person()
    "com.infinum.jsonapix.data.models.CompanyModel" -> TypeAdapter_Company()
    "com.infinum.jsonapix.data.models.DogModel" -> TypeAdapter_Dog()
    "com.infinum.jsonapix.data.models.AddressList" -> TypeAdapterList_Address()
    "com.infinum.jsonapix.data.models.PersonList" -> TypeAdapterList_Person()
    "com.infinum.jsonapix.data.models.CompanyList" -> TypeAdapterList_Company()
    "com.infinum.jsonapix.data.models.DogList" -> TypeAdapterList_Dog()
    else -> null
  }
}
