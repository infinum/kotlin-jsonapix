package com.infinum.jsonapix.processor.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class JsonApiXSymbolProcessorProvider : SymbolProcessorProvider {

    // The processor doesn't opt into KSP's new features, so @HasOne and @HasMany, which target fields,
    // stay on the property declarations, where the collector reads them.
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        JsonApiXSymbolProcessor(environment.codeGenerator, environment.logger)
}
