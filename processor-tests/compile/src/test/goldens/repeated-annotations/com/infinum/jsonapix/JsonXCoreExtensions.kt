@file:JvmName("JsonXExtensions")
@file:JvmMultifileClass

package com.infinum.jsonapix

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.discriminators.JsonApiDiscriminator
import com.infinum.jsonapix.core.discriminators.JsonApiListDiscriminator
import com.infinum.jsonapix.core.discriminators.TypeExtractor
import com.infinum.jsonapix.core.discriminators.TypeExtractor.findType
import com.infinum.jsonapix.core.discriminators.TypeExtractor.guessType
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Errors
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.retrofit.JsonXHttpException
import java.lang.IllegalArgumentException
import kotlin.Function1
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import retrofit2.HttpException

public inline fun <reified Model> Collection<Model>.toManyRelationshipModel(type: String, idMapper: Function1<Model, String> = { "" }): ManyRelationshipMember = ManyRelationshipMember(data = map { ResourceIdentifier(type, idMapper(it)) })

public inline fun <reified Model> Model.toOneRelationshipModel(type: String, id: String = ""): OneRelationshipMember = OneRelationshipMember(data = ResourceIdentifier(type, id))

public inline fun <reified Model : Error> decodeJsonApiError(errorBody: String): List<Model> = try {
  format.decodeFromString<Errors<Model>>(errorBody).errors
} catch (e: IllegalArgumentException) {
  emptyList()
}

public inline fun <reified Model : Error> HttpException.asJsonXHttpException(): JsonXHttpException<Model> {
  val response = response()
  val body = response?.errorBody()?.charStream()?.readText()
  val errors = if (body != null) decodeJsonApiError<Model>(body) else emptyList()
  return JsonXHttpException(response, errors)
}

public inline fun <reified Data, reified Model> String.decodeJsonApiXString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): JsonApiX<Data, Model> {
  val type = TypeExtractor.findType(Json.parseToJsonElement(this).jsonObject["data"])
  val discriminator = JsonApiDiscriminator(type ?: TypeExtractor.guessType(Model::class), rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonElement = Json.parseToJsonElement(this)
  val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()
  return format.decodeFromString<JsonApiX<Data,Model>>(jsonStringWithDiscriminator)
}

public inline fun <reified Data, reified Model> String.decodeJsonApiXListString(
  rootLinks: String,
  resourceObjectLinks: String,
  relationshipsLinks: String,
  rootMeta: String,
  resourceObjectMeta: String,
  relationshipsMeta: String,
  errors: String,
): JsonApiXList<Data, Model> {
  val de = Json.parseToJsonElement(this).jsonObject["data"]
  val type = if((de as? kotlinx.serialization.json.JsonArray)?.size == 0) TypeExtractor.guessType(Data::class) else TypeExtractor.findType(de)
  val discriminator = JsonApiListDiscriminator(type ?: TypeExtractor.guessType(Model::class), rootLinks, resourceObjectLinks, relationshipsLinks, rootMeta, resourceObjectMeta, relationshipsMeta, errors)
  val jsonElement = Json.parseToJsonElement(this)
  val jsonStringWithDiscriminator = discriminator.inject(jsonElement).toString()
  return format.decodeFromString<JsonApiXList<Data,Model>>(jsonStringWithDiscriminator)
}
