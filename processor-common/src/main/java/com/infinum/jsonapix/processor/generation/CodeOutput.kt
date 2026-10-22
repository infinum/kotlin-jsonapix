package com.infinum.jsonapix.processor.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

/**
 * Writes generated files for a backend. [originatingModels] are the class names of the models the
 * file was generated from, which the backend maps back to its own origins for incremental processing.
 */
public interface CodeOutput {

    public fun write(fileSpec: FileSpec, originatingModels: Set<ClassName>)
}
