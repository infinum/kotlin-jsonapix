package com.infinum.jsonapix.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WorkingSample {

    inline fun <reified T> T.toJsonApiString(type: String): String {
        return Json.encodeToString(this)
    }

    inline fun <reified T> String.decodeFromJsonApi(): T {
        return Json.decodeFromString(this)
    }


    // Sample app
    @Serializable
    data class Dummy(val name: String)
}