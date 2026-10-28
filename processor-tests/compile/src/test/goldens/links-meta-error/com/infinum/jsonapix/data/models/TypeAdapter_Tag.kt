package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Tag : TypeAdapter<TagModel> {
  override fun convertToString(input: TagModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): TagModel {
    val data = input.decodeJsonApiXString<Tag,TagModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootLinks(): String = "com.infinum.jsonapix.data.models.TagLinks"
}
