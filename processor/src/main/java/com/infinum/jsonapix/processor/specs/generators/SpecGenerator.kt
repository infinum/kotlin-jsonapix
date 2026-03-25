package com.infinum.jsonapix.processor.specs.generators

import java.io.File

internal interface SpecGenerator {
    fun generate(outputDir: File)
}
