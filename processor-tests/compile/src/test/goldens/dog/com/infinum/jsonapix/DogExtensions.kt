@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Dog
import com.infinum.jsonapix.`data`.models.Dog
import com.infinum.jsonapix.`data`.models.DogItem
import com.infinum.jsonapix.`data`.models.DogList
import com.infinum.jsonapix.`data`.models.DogModel
import com.infinum.jsonapix.`data`.models.JsonApiXList_Dog
import com.infinum.jsonapix.`data`.models.JsonApiX_Dog
import com.infinum.jsonapix.`data`.models.ResourceObject_Dog
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.core.shared.flatMapSafe
import com.infinum.jsonapix.core.shared.mapSafe
import kotlin.String
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

public fun Dog.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Dog = ResourceObject_Dog(id = id() ?: "0", attributes = Attributes_Dog.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun DogModel.toResourceObject(): ResourceObject_Dog = ResourceObject_Dog(id = data?.id() ?: "0", attributes = Attributes_Dog.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun DogItem.toResourceObject(): ResourceObject_Dog = ResourceObject_Dog(id = data?.id() ?: "0", attributes = Attributes_Dog.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun DogModel.toJsonApiX(): JsonApiX_Dog = JsonApiX_Dog(data = toResourceObject(), included = listOfNotNull(), meta = rootMeta, links = rootLinks)

public fun DogList.toJsonApiXList(): JsonApiXList_Dog = JsonApiXList_Dog(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull())

public fun DogModel.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiX()
  val type = jsonX.data.type
  val discriminator = JsonApiDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiX::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}

@JvmName("toJsonApiXStringDog")
public fun DogList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Dog::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
