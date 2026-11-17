package com.infinum.jsonapix.processor

import com.infinum.jsonapix.processor.generation.CompileClasspath
import com.squareup.kotlinpoet.ClassName
import javax.lang.model.util.Elements

/** Looks classes up through javac's element utilities, which only find classes on the compile classpath. */
internal class KaptCompileClasspath(
    private val elementUtils: Elements,
) : CompileClasspath {

    override fun hasClass(className: ClassName): Boolean =
        elementUtils.getTypeElement(className.canonicalName) != null
}
