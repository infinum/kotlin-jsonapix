package com.infinum.jsonapix.processor.specs

import com.squareup.kotlinpoet.ClassName

/** Classes that generated code refers to, shared between spec builders. */
internal object ClassNames {

    /**
     * Kotlin's `IllegalArgumentException`. `IllegalArgumentException::class.asClassName()` would name
     * `java.lang.IllegalArgumentException`, which generated code can't import outside the JVM.
     */
    val illegalArgumentException = ClassName("kotlin", "IllegalArgumentException")
}
