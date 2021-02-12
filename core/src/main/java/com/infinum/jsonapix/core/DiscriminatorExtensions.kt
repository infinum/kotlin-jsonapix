package com.infinum.jsonapix.core

import kotlinx.serialization.json.*

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
    val dataObject = element.jsonObject["data"]
    if (dataObject != null) {
        val dataEntries = dataObject.jsonObject.entries.toMutableSet()
        dataEntries.add(entry)
        resultMap["data"] = JsonObject(mapOf(*dataEntries.map { Pair(it.key, it.value) }.toTypedArray()))
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
    val dataObject = element.jsonObject["data"]
    if (dataObject != null) {
        val dataEntries = dataObject.jsonObject.entries.toMutableSet()
        dataEntries.removeAll { it.key == discriminatorName }
        resultMap["data"] = JsonObject(mapOf(*dataEntries.map { Pair(it.key, it.value) }.toTypedArray()))
    }
    val newJson = JsonObject(resultMap)
    return newJson.toString()
}

fun String.findType(): String {
    try {
        val element = Json.parseToJsonElement(this)
        val data = element.jsonObject["data"]
        val type = data?.jsonObject?.get("type")
        return type?.jsonPrimitive?.content!!
    } catch (e: Exception) {
        return try {
            val element = Json.parseToJsonElement(this)
            val data = element.jsonObject["data"]
            val first = data?.jsonArray?.get(0)
            val type = first?.jsonObject?.get("type")
            type?.jsonPrimitive?.content!!
        } catch (e: Exception) {
            throw e
        }
    }
}