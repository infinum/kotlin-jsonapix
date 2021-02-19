package com.infinum.jsonapix.processor.discriminators

import kotlinx.serialization.json.JsonElement

interface Discriminator {

    fun inject(jsonElement: JsonElement): JsonElement

    fun extract(jsonElement: JsonElement): JsonElement
}