package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Dog : TypeAdapter<DogModel> {
  override fun convertToString(input: DogModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): DogModel {
    val data = input.decodeJsonApiXString<Dog,DogModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }
}
