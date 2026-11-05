package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.AuthorList
import com.infinum.jsonapix.`data`.models.AuthorModel
import com.infinum.jsonapix.`data`.models.BookList
import com.infinum.jsonapix.`data`.models.BookModel
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Author
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Book
import com.infinum.jsonapix.`data`.models.TypeAdapter_Author
import com.infinum.jsonapix.`data`.models.TypeAdapter_Book
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    AuthorModel::class -> TypeAdapter_Author()
    BookModel::class -> TypeAdapter_Book()
    AuthorList::class -> TypeAdapterList_Author()
    BookList::class -> TypeAdapterList_Book()
    else -> null
  }
}
