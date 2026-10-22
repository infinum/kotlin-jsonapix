package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_Dog : TypeAdapter<DogList> {
  override fun convertToString(input: DogList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): DogList {
    val data = input.decodeJsonApiXListString<Dog,DogList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }
}
