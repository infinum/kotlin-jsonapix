package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.AddressList
import com.infinum.jsonapix.`data`.models.AddressModel
import com.infinum.jsonapix.`data`.models.CompanyList
import com.infinum.jsonapix.`data`.models.CompanyModel
import com.infinum.jsonapix.`data`.models.DogList
import com.infinum.jsonapix.`data`.models.DogModel
import com.infinum.jsonapix.`data`.models.PersonList
import com.infinum.jsonapix.`data`.models.PersonModel
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
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    AddressModel::class -> TypeAdapter_Address()
    CompanyModel::class -> TypeAdapter_Company()
    DogModel::class -> TypeAdapter_Dog()
    PersonModel::class -> TypeAdapter_Person()
    AddressList::class -> TypeAdapterList_Address()
    CompanyList::class -> TypeAdapterList_Company()
    DogList::class -> TypeAdapterList_Dog()
    PersonList::class -> TypeAdapterList_Person()
    else -> null
  }
}
