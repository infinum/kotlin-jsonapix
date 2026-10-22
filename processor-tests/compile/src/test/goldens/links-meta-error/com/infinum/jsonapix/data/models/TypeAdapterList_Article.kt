package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_Article : TypeAdapter<ArticleList> {
  override fun convertToString(input: ArticleList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): ArticleList {
    val data = input.decodeJsonApiXListString<Article,ArticleList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootLinks(): String = "com.infinum.jsonapix.data.models.ArticleRootLinks"

  override fun resourceObjectLinks(): String = "com.infinum.jsonapix.data.models.ArticleResourceLinks"

  override fun relationshipsLinks(): String = "com.infinum.jsonapix.data.models.ArticleRelationshipsLinks"

  override fun rootMeta(): String = "com.infinum.jsonapix.data.models.ArticleRootMeta"

  override fun resourceObjectMeta(): String = "com.infinum.jsonapix.data.models.ArticleResourceMeta"

  override fun relationshipsMeta(): String = "com.infinum.jsonapix.data.models.ArticleRelationshipsMeta"

  override fun errors(): String = "com.infinum.jsonapix.data.models.ArticleError"
}
