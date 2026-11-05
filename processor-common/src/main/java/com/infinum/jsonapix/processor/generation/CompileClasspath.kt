package com.infinum.jsonapix.processor.generation

import com.squareup.kotlinpoet.ClassName

/**
 * Looks up classes on the compile classpath of the module being processed. That's what the generated
 * code compiles against, unlike the processor's own classpath, which always has every JsonApiX artifact.
 */
public interface CompileClasspath {

    public fun hasClass(className: ClassName): Boolean
}
