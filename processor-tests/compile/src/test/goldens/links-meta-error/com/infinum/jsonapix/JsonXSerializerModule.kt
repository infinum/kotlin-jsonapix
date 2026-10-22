@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.ArticleError
import com.infinum.jsonapix.`data`.models.ArticleRelationshipsLinks
import com.infinum.jsonapix.`data`.models.ArticleRelationshipsMeta
import com.infinum.jsonapix.`data`.models.ArticleResourceLinks
import com.infinum.jsonapix.`data`.models.ArticleResourceMeta
import com.infinum.jsonapix.`data`.models.ArticleRootLinks
import com.infinum.jsonapix.`data`.models.ArticleRootMeta
import com.infinum.jsonapix.`data`.models.Attributes_Article
import com.infinum.jsonapix.`data`.models.Attributes_Comment
import com.infinum.jsonapix.`data`.models.Attributes_Tag
import com.infinum.jsonapix.`data`.models.CommentError
import com.infinum.jsonapix.`data`.models.JsonApiXList_Article
import com.infinum.jsonapix.`data`.models.JsonApiXList_Comment
import com.infinum.jsonapix.`data`.models.JsonApiXList_Tag
import com.infinum.jsonapix.`data`.models.JsonApiX_Article
import com.infinum.jsonapix.`data`.models.JsonApiX_Comment
import com.infinum.jsonapix.`data`.models.JsonApiX_Tag
import com.infinum.jsonapix.`data`.models.Relationships_Article
import com.infinum.jsonapix.`data`.models.ResourceObject_Article
import com.infinum.jsonapix.`data`.models.ResourceObject_Comment
import com.infinum.jsonapix.`data`.models.ResourceObject_Tag
import com.infinum.jsonapix.`data`.models.TagLinks
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.resources.Attributes
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.Relationships
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.resources.UnknownMeta
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

public val jsonApiXSerializerModule: SerializersModule = SerializersModule {
  polymorphic(JsonApiX::class) {
    subclass(JsonApiX_Comment::class)
    subclass(JsonApiX_Tag::class)
    subclass(JsonApiX_Article::class)
  }
  polymorphic(JsonApiXList::class) {
    subclass(JsonApiXList_Comment::class)
    subclass(JsonApiXList_Tag::class)
    subclass(JsonApiXList_Article::class)
  }
  polymorphic(ResourceObject::class) {
    subclass(ResourceObject_Comment::class)
    subclass(ResourceObject_Tag::class)
    subclass(ResourceObject_Article::class)
  }
  polymorphic(Attributes::class) {
    subclass(Attributes_Comment::class)
    subclass(Attributes_Tag::class)
    subclass(Attributes_Article::class)
  }
  polymorphic(Relationships::class) {
    subclass(Relationships_Article::class)
  }
  polymorphic(Links::class) {
    subclass(DefaultLinks::class)
    subclass(ArticleResourceLinks::class)
    subclass(TagLinks::class)
    subclass(ArticleRelationshipsLinks::class)
    subclass(ArticleRootLinks::class)
  }
  polymorphic(Error::class) {
    subclass(DefaultError::class)
    subclass(CommentError::class)
    subclass(ArticleError::class)
  }
  polymorphic(Meta::class) {
    subclass(ArticleResourceMeta::class)
    subclass(ArticleRelationshipsMeta::class)
    subclass(ArticleRootMeta::class)
    defaultDeserializer{ UnknownMeta.serializer() }
  }
  contextual(OneRelationshipMember.serializer())
  contextual(ManyRelationshipMember.serializer())
  contextual(ResourceIdentifier.serializer())
}

public val format: Json = Json {
  encodeDefaults = true
  classDiscriminator = "#class"
  serializersModule = jsonApiXSerializerModule
  ignoreUnknownKeys = true
}
