package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.specbuilders.TypeAdapterFactorySpecBuilder
import java.io.File

internal class TypeAdapterFactorySpecGenerator(
    private val holders: Set<JsonApiXHolder>
) : SpecGenerator {

    override fun generate(outputDir: File) {
        TypeAdapterFactorySpecBuilder().build(holders.map { it.className }).writeTo(outputDir)
    }
}
