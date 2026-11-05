@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.`data`.models.Attributes_Book
import com.infinum.jsonapix.`data`.models.Book
import com.infinum.jsonapix.`data`.models.BookItem
import com.infinum.jsonapix.`data`.models.BookLinks
import com.infinum.jsonapix.`data`.models.BookList
import com.infinum.jsonapix.`data`.models.BookMeta
import com.infinum.jsonapix.`data`.models.BookModel
import com.infinum.jsonapix.`data`.models.JsonApiXList_Book
import com.infinum.jsonapix.`data`.models.JsonApiX_Book
import com.infinum.jsonapix.`data`.models.Relationships_Book
import com.infinum.jsonapix.`data`.models.ResourceObject_Book
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
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

public fun Book.toResourceObject(meta: Meta?, links: Links?): ResourceObject_Book = ResourceObject_Book(id = id() ?: "0", attributes = Attributes_Book.fromOriginalObject(this), relationships = Relationships_Book.fromOriginalObject(this), meta = meta as? BookMeta, links = links as? BookLinks)

public fun BookModel.toResourceObject(): ResourceObject_Book = ResourceObject_Book(id = data?.id() ?: "0", attributes = Attributes_Book.fromOriginalObject(data), relationships = Relationships_Book.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun BookItem.toResourceObject(): ResourceObject_Book = ResourceObject_Book(id = data?.id() ?: "0", attributes = Attributes_Book.fromOriginalObject(data), relationships = Relationships_Book.fromOriginalObject(data), meta = resourceObjectMeta, links = resourceObjectLinks)

public fun BookModel.toJsonApiX(): JsonApiX_Book = JsonApiX_Book(data = toResourceObject(), included = listOfNotNull(data.author?.let{it.toResourceObject(
                    relationshipsMeta?.get("author"),
                    relationshipsLinks?.get("author")
                )}), meta = rootMeta, links = rootLinks)

public fun BookList.toJsonApiXList(): JsonApiXList_Book = JsonApiXList_Book(
                meta = rootMeta,
                links = rootLinks,
                errors = errors,
                data =data?.map { it.toResourceObject() }?.filterNotNull().orEmpty(), included = listOfNotNull(*data.mapSafe {item ->
                    item.data.author?.let{it.toResourceObject(
                        item.relationshipsMeta?.get("author"),
                        item.relationshipsLinks?.get("author")
                    )} }.toTypedArray()))

public fun BookModel.toJsonApiXString(
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

@JvmName("toJsonApiXStringBook")
public fun BookList.toJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): String {
  val jsonX = this.toJsonApiXList()
  val type = jsonX.data.firstOrNull()?.type ?: TypeExtractor.guessType(Book::class)
  val discriminator = JsonApiListDiscriminator(type, rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonString = format.encodeToString(PolymorphicSerializer(JsonApiXList::class), jsonX)
  return discriminator.extract(Json.parseToJsonElement(jsonString)).toString()
}
