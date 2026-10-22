package com.infinum.jsonapix.processor.specs.generators

import com.squareup.kotlinpoet.FileSpec

internal interface SpecGenerator {
    fun generate(): List<FileSpec>
}
