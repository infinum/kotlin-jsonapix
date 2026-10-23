@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Owner
import com.infinum.jsonapix.`data`.models.JsonApiXList_Owner
import com.infinum.jsonapix.`data`.models.JsonApiX_Owner
import com.infinum.jsonapix.`data`.models.Owner
import com.infinum.jsonapix.`data`.models.OwnerItem
import com.infinum.jsonapix.`data`.models.OwnerList
import com.infinum.jsonapix.`data`.models.OwnerModel
import com.infinum.jsonapix.`data`.models.Relationships_Owner
import com.infinum.jsonapix.`data`.models.ResourceObject_Owner
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

public fun Owner.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Owner = ResourceObject_Owner(id = id() ?: "0", attributes = Attributes_Owner.fromOriginalObject(this), relationships = Relationships_Owner.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun OwnerModel.toResourceObject(): ResourceObject_Owner = ResourceObject_Owner(id = data?.id() ?: "0", attributes = Attributes_Owner.fromOriginalObject(data), relationships = Relationships_Owner.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun OwnerItem.toResourceObject(): ResourceObject_Owner = ResourceObject_Owner(id = data?.id() ?: "0", attributes = Attributes_Owner.fromOriginalObject(data), relationships = Relationships_Owner.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun OwnerModel.toJsonApiX(): JsonApiX_Owner = JsonApiX_Owner(data = toResourceObject(), included = listOfNotNull(data.favouritePet?.let{it.toResourceObject(
                    relationshipsMeta?.get("favouritePet"),
                    relationshipsLinks?.get("favouritePet")
                )}, *data.pets.mapSafe { it.let{it.toResourceObject(
                        relationshipsMeta?.get("pets"),
                        relationshipsLinks?.get("pets")
                    )}}.toTypedArray()), meta = rootMeta, links = rootLinks)

public fun OwnerList.toJsonApiXList(): JsonApiXList_Owner = JsonApiXList_Owner(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull(*data.mapSafe {item ->
                    item.data.favouritePet?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("favouritePet"),
                        item.relationshipsLinks?.get("favouritePet")
                    )} }.toTypedArray(),*data.flatMapSafe {item-> item.data.pets.mapSafe {
it.toResourceObject(
                     item.relationshipsMeta?.get("pets"),
                     item.relationshipsLinks?.get("pets")
                )} }.toTypedArray()))

public fun OwnerModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringOwner")
public fun OwnerList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Owner::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
