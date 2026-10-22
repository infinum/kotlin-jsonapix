package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Comment : TypeAdapter<CommentModel> {
  override fun convertToString(input: CommentModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): CommentModel {
    val data = input.decodeJsonApiXString<Comment,CommentModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun errors(): String = "com.infinum.jsonapix.data.models.CommentError"
}
