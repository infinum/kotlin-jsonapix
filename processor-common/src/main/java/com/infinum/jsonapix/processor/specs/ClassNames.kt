package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.retrofit.JsonXHttpException
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

/** Classes that generated code refers to, shared between spec builders. */
internal object ClassNames {

    /**
     * Kotlin's `IllegalArgumentException`. `IllegalArgumentException::class.asClassName()` would name
     * `java.lang.IllegalArgumentException`, which generated code can't import outside the JVM.
     */
    val illegalArgumentException = ClassName("kotlin", "IllegalArgumentException")

    /**
     * What the Retrofit extension returns, from JsonApiX's `retrofit` artifact. The processor itself always
     * has it, so the generator looks it up on the consumer's compile classpath to decide whether to generate
     * the extension. A KMP `commonMain` compilation never has it.
     */
    val jsonXHttpException = JsonXHttpException::class.asClassName()
}
