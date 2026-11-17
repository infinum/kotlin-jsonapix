@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Author
import com.infinum.jsonapix.`data`.models.Attributes_Book
import com.infinum.jsonapix.`data`.models.BookLinks
import com.infinum.jsonapix.`data`.models.BookMeta
import com.infinum.jsonapix.`data`.models.JsonApiXList_Author
import com.infinum.jsonapix.`data`.models.JsonApiXList_Book
import com.infinum.jsonapix.`data`.models.JsonApiX_Author
import com.infinum.jsonapix.`data`.models.JsonApiX_Book
import com.infinum.jsonapix.`data`.models.LibraryError
import com.infinum.jsonapix.`data`.models.Relationships_Book
import com.infinum.jsonapix.`data`.models.ResourceObject_Author
import com.infinum.jsonapix.`data`.models.ResourceObject_Book
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
    subclass(JsonApiX_Author::class)
    subclass(JsonApiX_Book::class)
  }
  polymorphic(JsonApiXList::class) {
    subclass(JsonApiXList_Author::class)
    subclass(JsonApiXList_Book::class)
  }
  polymorphic(ResourceObject::class) {
    subclass(ResourceObject_Author::class)
    subclass(ResourceObject_Book::class)
  }
  polymorphic(Attributes::class) {
    subclass(Attributes_Author::class)
    subclass(Attributes_Book::class)
  }
  polymorphic(Relationships::class) {
    subclass(Relationships_Book::class)
  }
  polymorphic(Links::class) {
    subclass(DefaultLinks::class)
    subclass(BookLinks::class)
    subclass(BookLinks::class)
    subclass(BookLinks::class)
  }
  polymorphic(Error::class) {
    subclass(DefaultError::class)
    subclass(LibraryError::class)
    subclass(LibraryError::class)
  }
  polymorphic(Meta::class) {
    subclass(BookMeta::class)
    subclass(BookMeta::class)
    subclass(BookMeta::class)
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
