package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.TypeAdapterFactorySpecBuilder
import com.squareup.kotlinpoet.FileSpec

internal class TypeAdapterFactorySpecGenerator(
    private val holders: Set<JsonApiXHolder>,
) : SpecGenerator {

    override fun generate(): List<FileSpec> =
        listOf(TypeAdapterFactorySpecBuilder().build(holders.map { it.className }))
}
