package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_Author : TypeAdapter<AuthorList> {
  override fun convertToString(input: AuthorList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): AuthorList {
    val data = input.decodeJsonApiXListString<Author,AuthorList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun errors(): String = "com.infinum.jsonapix.data.models.LibraryError"
}
