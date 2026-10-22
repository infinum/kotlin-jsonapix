package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Article : TypeAdapter<ArticleModel> {
  override fun convertToString(input: ArticleModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): ArticleModel {
    val data = input.decodeJsonApiXString<Article,ArticleModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
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
