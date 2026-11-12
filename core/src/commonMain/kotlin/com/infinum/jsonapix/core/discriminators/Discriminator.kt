package com.infinum.jsonapix.core.discriminators

import kotlinx.serialization.json.JsonElement

/**
 * An interface to implement classes that will inject and extract class discriminator strings
 * from serialized/deserialized objects
 */
interface Discriminator {

    fun inject(jsonElement: JsonElement): JsonElement

    fun extract(jsonElement: JsonElement): JsonElement
}
