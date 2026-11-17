package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.ArticleList
import com.infinum.jsonapix.`data`.models.ArticleModel
import com.infinum.jsonapix.`data`.models.CommentList
import com.infinum.jsonapix.`data`.models.CommentModel
import com.infinum.jsonapix.`data`.models.TagList
import com.infinum.jsonapix.`data`.models.TagModel
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
  override fun getAdapter(type: KClass<out Any>): TypeAdapter<out Any>? = when(type) {
    ArticleModel::class -> TypeAdapter_Article()
    CommentModel::class -> TypeAdapter_Comment()
    TagModel::class -> TypeAdapter_Tag()
    ArticleList::class -> TypeAdapterList_Article()
    CommentList::class -> TypeAdapterList_Comment()
    TagList::class -> TypeAdapterList_Tag()
    else -> null
  }
}
