@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Owner
import com.infinum.jsonapix.`data`.models.Attributes_Pet
import com.infinum.jsonapix.`data`.models.JsonApiXList_Owner
import com.infinum.jsonapix.`data`.models.JsonApiXList_Pet
import com.infinum.jsonapix.`data`.models.JsonApiX_Owner
import com.infinum.jsonapix.`data`.models.JsonApiX_Pet
import com.infinum.jsonapix.`data`.models.Relationships_Owner
import com.infinum.jsonapix.`data`.models.ResourceObject_Owner
import com.infinum.jsonapix.`data`.models.ResourceObject_Pet
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
    subclass(JsonApiX_Owner::class)
    subclass(JsonApiX_Pet::class)
  }
  polymorphic(JsonApiXList::class) {
    subclass(JsonApiXList_Owner::class)
    subclass(JsonApiXList_Pet::class)
  }
  polymorphic(ResourceObject::class) {
    subclass(ResourceObject_Owner::class)
    subclass(ResourceObject_Pet::class)
  }
  polymorphic(Attributes::class) {
    subclass(Attributes_Owner::class)
    subclass(Attributes_Pet::class)
  }
  polymorphic(Relationships::class) {
    subclass(Relationships_Owner::class)
  }
  polymorphic(Links::class) {
    subclass(DefaultLinks::class)
  }
  polymorphic(Error::class) {
    subclass(DefaultError::class)
  }
  polymorphic(Meta::class) {
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
