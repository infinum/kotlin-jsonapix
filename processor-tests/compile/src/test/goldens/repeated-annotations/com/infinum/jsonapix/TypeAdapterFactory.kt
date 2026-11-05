package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.TypeAdapterList_Author
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Book
import com.infinum.jsonapix.`data`.models.TypeAdapter_Author
import com.infinum.jsonapix.`data`.models.TypeAdapter_Book
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type.qualifiedName) {
    "com.infinum.jsonapix.data.models.AuthorModel" -> TypeAdapter_Author()
    "com.infinum.jsonapix.data.models.BookModel" -> TypeAdapter_Book()
    "com.infinum.jsonapix.data.models.AuthorList" -> TypeAdapterList_Author()
    "com.infinum.jsonapix.data.models.BookList" -> TypeAdapterList_Book()
    else -> null
  }
}
