@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Address
import com.infinum.jsonapix.`data`.models.AddressItem
import com.infinum.jsonapix.`data`.models.AddressList
import com.infinum.jsonapix.`data`.models.AddressModel
import com.infinum.jsonapix.`data`.models.Attributes_Address
import com.infinum.jsonapix.`data`.models.JsonApiXList_Address
import com.infinum.jsonapix.`data`.models.JsonApiX_Address
import com.infinum.jsonapix.`data`.models.ResourceObject_Address
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

public fun Address.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Address = ResourceObject_Address(id = id() ?: "0", attributes = Attributes_Address.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun AddressModel.toResourceObject(): ResourceObject_Address = ResourceObject_Address(id = data?.id() ?: "0", attributes = Attributes_Address.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun AddressItem.toResourceObject(): ResourceObject_Address = ResourceObject_Address(id = data?.id() ?: "0", attributes = Attributes_Address.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun AddressModel.toJsonApiX(): JsonApiX_Address = JsonApiX_Address(data = toResourceObject(), included = listOfNotNull(), meta = rootMeta, links = rootLinks)

public fun AddressList.toJsonApiXList(): JsonApiXList_Address = JsonApiXList_Address(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull())

public fun AddressModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringAddress")
public fun AddressList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Address::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
