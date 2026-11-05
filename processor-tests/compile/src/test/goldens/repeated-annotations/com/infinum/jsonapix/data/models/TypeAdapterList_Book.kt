package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_Book : TypeAdapter<BookList> {
  override fun convertToString(input: BookList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): BookList {
    val data = input.decodeJsonApiXListString<Book,BookList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootLinks(): String = "com.infinum.jsonapix.data.models.BookLinks"

  override fun resourceObjectLinks(): String = "com.infinum.jsonapix.data.models.BookLinks"

  override fun relationshipsLinks(): String = "com.infinum.jsonapix.data.models.BookLinks"

  override fun rootMeta(): String = "com.infinum.jsonapix.data.models.BookMeta"

  override fun resourceObjectMeta(): String = "com.infinum.jsonapix.data.models.BookMeta"

  override fun relationshipsMeta(): String = "com.infinum.jsonapix.data.models.BookMeta"

  override fun errors(): String = "com.infinum.jsonapix.data.models.LibraryError"
}
