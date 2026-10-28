@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Person
import com.infinum.jsonapix.`data`.models.JsonApiXList_Person
import com.infinum.jsonapix.`data`.models.JsonApiX_Person
import com.infinum.jsonapix.`data`.models.Person
import com.infinum.jsonapix.`data`.models.PersonItem
import com.infinum.jsonapix.`data`.models.PersonList
import com.infinum.jsonapix.`data`.models.PersonModel
import com.infinum.jsonapix.`data`.models.PersonResourceMeta
import com.infinum.jsonapix.`data`.models.Relationships_Person
import com.infinum.jsonapix.`data`.models.ResourceObject_Person
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

public fun Person.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Person = ResourceObject_Person(id = id() ?: "0", attributes = Attributes_Person.fromOriginalObject(this), relationships = Relationships_Person.fromOriginalObject(this), meta = meta as? PersonResourceMeta, links = links as? DefaultLinks)

public fun PersonModel.toResourceObject(): ResourceObject_Person = ResourceObject_Person(id = data?.id() ?: "0", attributes = Attributes_Person.fromOriginalObject(data), relationships = Relationships_Person.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun PersonItem.toResourceObject(): ResourceObject_Person = ResourceObject_Person(id = data?.id() ?: "0", attributes = Attributes_Person.fromOriginalObject(data), relationships = Relationships_Person.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun PersonModel.toJsonApiX(): JsonApiX_Person = JsonApiX_Person(data = toResourceObject(), included = listOfNotNull(data.myFavoriteDog?.let{it.toResourceObject(
                    relationshipsMeta?.get("myFavoriteDog"),
                    relationshipsLinks?.get("myFavoriteDog")
                )}, *data.allMyDogs.mapSafe { it.let{it.toResourceObject(
                        relationshipsMeta?.get("allMyDogs"),
                        relationshipsLinks?.get("allMyDogs")
                    )}}.toTypedArray()), meta = rootMeta, links = rootLinks)

public fun PersonList.toJsonApiXList(): JsonApiXList_Person = JsonApiXList_Person(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull(*data.mapSafe {item ->
                    item.data.myFavoriteDog?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("myFavoriteDog"),
                        item.relationshipsLinks?.get("myFavoriteDog")
                    )} }.toTypedArray(),*data.flatMapSafe {item-> item.data.allMyDogs.mapSafe {
it.toResourceObject(
                     item.relationshipsMeta?.get("allMyDogs"),
                     item.relationshipsLinks?.get("allMyDogs")
                )} }.toTypedArray()))

public fun PersonModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringPerson")
public fun PersonList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Person::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
