package com.infinum.jsonapix.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO Find a way to make functions that will parse the annotated classes and annotated classes only. Keep commented section as a reference
//inline fun <reified T> T.toJsonApiString(type: String): String {
//    return Json.encodeToString(this)
//}
//
//inline fun <reified T> String.decodeFromJsonApi(): T {
//    return Json.decodeFromString(this)
//    //return Json.decodeFromString<JsonApiWrapper<T>>(this).data
//}