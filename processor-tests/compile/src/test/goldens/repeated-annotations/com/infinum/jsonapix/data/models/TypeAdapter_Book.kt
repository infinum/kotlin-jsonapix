package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Book : TypeAdapter<BookModel> {
  override fun convertToString(input: BookModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): BookModel {
    val data = input.decodeJsonApiXString<Book,BookModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
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
