@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Company
import com.infinum.jsonapix.`data`.models.CompanyItem
import com.infinum.jsonapix.`data`.models.CompanyList
import com.infinum.jsonapix.`data`.models.CompanyModel
import com.infinum.jsonapix.`data`.models.JsonApiXList_Company
import com.infinum.jsonapix.`data`.models.JsonApiX_Company
import com.infinum.jsonapix.`data`.models.Relationships_Company
import com.infinum.jsonapix.`data`.models.ResourceObject_Company
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

public fun Company.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Company = ResourceObject_Company(id = id() ?: "0", relationships = Relationships_Company.fromOriginalObject(this), meta = meta as? Meta, links = links as? DefaultLinks)

public fun CompanyModel.toResourceObject(): ResourceObject_Company = ResourceObject_Company(id = data?.id() ?: "0", relationships = Relationships_Company.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun CompanyItem.toResourceObject(): ResourceObject_Company = ResourceObject_Company(id = data?.id() ?: "0", relationships = Relationships_Company.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun CompanyModel.toJsonApiX(): JsonApiX_Company = JsonApiX_Company(data = toResourceObject(), included = listOfNotNull(data.manager?.let{it.toResourceObject(
                    relationshipsMeta?.get("manager"),
                    relationshipsLinks?.get("manager")
                )}, data.address?.let{it.toResourceObject(
                    relationshipsMeta?.get("address"),
                    relationshipsLinks?.get("address")
                )}, *data.personel.mapSafe { it.let{it.toResourceObject(
                        relationshipsMeta?.get("personel"),
                        relationshipsLinks?.get("personel")
                    )}}.toTypedArray()), meta = rootMeta, links = rootLinks)

public fun CompanyList.toJsonApiXList(): JsonApiXList_Company = JsonApiXList_Company(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull(*data.mapSafe {item ->
                    item.data.manager?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("manager"),
                        item.relationshipsLinks?.get("manager")
                    )} }.toTypedArray(),*data.mapSafe {item ->
                    item.data.address?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("address"),
                        item.relationshipsLinks?.get("address")
                    )} }.toTypedArray(),*data.flatMapSafe {item-> item.data.personel.mapSafe {
it.toResourceObject(
                     item.relationshipsMeta?.get("personel"),
                     item.relationshipsLinks?.get("personel")
                )} }.toTypedArray()))

public fun CompanyModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringCompany")
public fun CompanyList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Company::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
