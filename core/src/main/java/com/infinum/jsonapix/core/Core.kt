package com.infinum.jsonapix.core

import kotlinx.serialization.json.Json


//inline fun <reified T> T.toJsonApiString(type: String): String {
//    return Json.encodeToString(this)
//}
//
//inline fun <reified T> String.decodeFromJsonApi(): T {
//    return Json.decodeFromString(this)
//    //return Json.decodeFromString<JsonApiWrapper<T>>(this).data
//}
//
//inline fun <reified T: JsonApiSerializable> T.getJsonApiSerializer(): JsonApiSerializer<T> {
//    return JsonApiSerializer(serializer())
//}