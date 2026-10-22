package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_EmptyData : TypeAdapter<EmptyDataList> {
  override fun convertToString(input: EmptyDataList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): EmptyDataList {
    val data = input.decodeJsonApiXListString<EmptyData,EmptyDataList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootMeta(): String = "com.infinum.jsonapix.data.models.EmptyDataMeta"
}
