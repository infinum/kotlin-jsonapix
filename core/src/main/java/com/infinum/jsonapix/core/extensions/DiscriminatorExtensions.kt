@file:SuppressWarnings("TooGenericExceptionCaught", "SpreadOperator")
package com.infinum.jsonapix.core.extensions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val DATA_KEY = "data"
private const val TYPE_KEY = "type"

fun String.injectClassDiscriminator(
    discriminatorName: String,
    discriminatorValue: String
): String {
    val element = Json.parseToJsonElement(this)
    val entries = element.jsonObject.entries.toMutableSet()
    val entry = object : Map.Entry<String, JsonElement> {
        override val key = discriminatorName
        override val value = JsonPrimitive(discriminatorValue)
    }
    entries.add(entry)
    val resultMap = mutableMapOf<String, JsonElement>()
    resultMap.putAll(entries.map { Pair(it.key, it.value) })

    val dataObject = element.jsonObject[DATA_KEY]
    if (dataObject != null) {
        val dataEntries = dataObject.jsonObject.entries.toMutableSet()
        dataEntries.add(entry)
        resultMap[DATA_KEY] = JsonObject(mapOf(*dataEntries.map { Pair(it.key, it.value) }.toTypedArray()))
    }
    val newJson = JsonObject(resultMap)
    return newJson.toString()
}

fun String.extractClassDiscriminator(
    discriminatorName: String
): String {
    val element = Json.parseToJsonElement(this)
    val entries = element.jsonObject.entries.toMutableSet()
    entries.removeAll { it.key == discriminatorName }
    val resultMap = mutableMapOf<String, JsonElement>()
    resultMap.putAll(entries.map { Pair(it.key, it.value) })
    val dataObject = element.jsonObject[DATA_KEY]
    if (dataObject != null) {
        val dataEntries = dataObject.jsonObject.entries.toMutableSet()
        dataEntries.removeAll { it.key == discriminatorName }
        resultMap[DATA_KEY] = JsonObject(mapOf(*dataEntries.map { Pair(it.key, it.value) }.toTypedArray()))
    }
    val newJson = JsonObject(resultMap)
    return newJson.toString()
}

fun String.findType(): String {
    try {
        val element = Json.parseToJsonElement(this)
        val data = element.jsonObject[DATA_KEY]
        val type = data?.jsonObject?.get(TYPE_KEY)
        return type?.jsonPrimitive?.content!!
    } catch (e: Exception) {
        return try {
            val element = Json.parseToJsonElement(this)
            val data = element.jsonObject[DATA_KEY]
            val first = data?.jsonArray?.get(0)
            val type = first?.jsonObject?.get(TYPE_KEY)
            type?.jsonPrimitive?.content!!
        } catch (e: Exception) {
            throw e
        }
    }
}
