package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.TypeAdapterList_Article
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Comment
import com.infinum.jsonapix.`data`.models.TypeAdapterList_Tag
import com.infinum.jsonapix.`data`.models.TypeAdapter_Article
import com.infinum.jsonapix.`data`.models.TypeAdapter_Comment
import com.infinum.jsonapix.`data`.models.TypeAdapter_Tag
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import kotlin.Any
import kotlin.reflect.KClass

public class TypeAdapterFactory : AdapterFactory {
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type.qualifiedName) {
    "com.infinum.jsonapix.data.models.ArticleModel" -> TypeAdapter_Article()
    "com.infinum.jsonapix.data.models.CommentModel" -> TypeAdapter_Comment()
    "com.infinum.jsonapix.data.models.TagModel" -> TypeAdapter_Tag()
    "com.infinum.jsonapix.data.models.ArticleList" -> TypeAdapterList_Article()
    "com.infinum.jsonapix.data.models.CommentList" -> TypeAdapterList_Comment()
    "com.infinum.jsonapix.data.models.TagList" -> TypeAdapterList_Tag()
    else -> null
  }
}
