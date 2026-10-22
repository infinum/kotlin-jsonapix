@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Dog
import com.infinum.jsonapix.`data`.models.Attributes_Person
import com.infinum.jsonapix.`data`.models.JsonApiXList_Dog
import com.infinum.jsonapix.`data`.models.JsonApiXList_Person
import com.infinum.jsonapix.`data`.models.JsonApiX_Dog
import com.infinum.jsonapix.`data`.models.JsonApiX_Person
import com.infinum.jsonapix.`data`.models.PersonRelationshipMeta
import com.infinum.jsonapix.`data`.models.PersonResourceMeta
import com.infinum.jsonapix.`data`.models.PersonRootMeta
import com.infinum.jsonapix.`data`.models.PersonalError
import com.infinum.jsonapix.`data`.models.Relationships_Person
import com.infinum.jsonapix.`data`.models.ResourceObject_Dog
import com.infinum.jsonapix.`data`.models.ResourceObject_Person
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
    subclass(JsonApiX_Dog::class)
    subclass(JsonApiX_Person::class)
  }
  polymorphic(JsonApiXList::class) {
    subclass(JsonApiXList_Dog::class)
    subclass(JsonApiXList_Person::class)
  }
  polymorphic(ResourceObject::class) {
    subclass(ResourceObject_Dog::class)
    subclass(ResourceObject_Person::class)
  }
  polymorphic(Attributes::class) {
    subclass(Attributes_Dog::class)
    subclass(Attributes_Person::class)
  }
  polymorphic(Relationships::class) {
    subclass(Relationships_Person::class)
  }
  polymorphic(Links::class) {
    subclass(DefaultLinks::class)
  }
  polymorphic(Error::class) {
    subclass(DefaultError::class)
    subclass(PersonalError::class)
  }
  polymorphic(Meta::class) {
    subclass(PersonRelationshipMeta::class)
    subclass(PersonResourceMeta::class)
    subclass(PersonRootMeta::class)
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
