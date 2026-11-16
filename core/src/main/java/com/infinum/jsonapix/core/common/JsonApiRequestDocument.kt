package com.infinum.jsonapix.core.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Top level members that JSON:API defines for responses only. A request document that carries
 * them is not a valid request document, and strict servers reject it.
 */
private val RESPONSE_ONLY_MEMBERS = setOf(
    JsonApiConstants.Keys.INCLUDED,
    JsonApiConstants.Keys.ERRORS,
)

/**
 * Reshapes a serialized JSON:API document into one that is valid to send as a request body.
 *
 * Serialization writes out every member of the document, including the ones that only make sense
 * in a response and the ones that were never set. This drops both:
 *
 * - the response only top level members `included` and `errors`
 * - every member whose value is JSON `null`, at any depth
 *
 * An unset member of a JsonApiX model is indistinguishable from one explicitly set to `null`, so
 * writing it out as `null` would tell the server to clear the value. Omitting it is the only
 * reading that matches the model.
 */
fun String.toJsonApiRequestDocument(): String {
    val document = Json.parseToJsonElement(this).jsonObject
    return JsonObject(document.filterKeys { it !in RESPONSE_ONLY_MEMBERS })
        .withoutNullMembers()
        .toString()
}

private fun JsonElement.withoutNullMembers(): JsonElement = when (this) {
    is JsonObject -> JsonObject(
        entries
            .filter { it.value !is JsonNull }
            .associate { it.key to it.value.withoutNullMembers() },
    )

    // Array entries are positional, so a null entry is dropped only when it is a member of an
    // object nested inside the array.
    is JsonArray -> JsonArray(map { it.withoutNullMembers() })

    else -> this
}
