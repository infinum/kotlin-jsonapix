package com.infinum.jsonapix.core

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

inline fun <reified T> T.toJsonApiString(type: String): String {
    return Json.encodeToString(this)
    //return Json.encodeToString(JsonApiWrapper(type, this))
}

inline fun <reified T> String.decodeFromJsonApi(): T {
    return Json.decodeFromString(this)
    //return Json.decodeFromString<JsonApiWrapper<T>>(this).data
}

@Serializable//(with = JsonApiSerializer::class)
class JsonApiWrapper<out T>(
    @SerialName("type") val type: String,
    @SerialName("data") val data: T
) {
}

interface JsonApiSerializable {
    val dataType: String
}