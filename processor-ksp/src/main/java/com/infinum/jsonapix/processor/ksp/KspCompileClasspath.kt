package com.infinum.jsonapix.processor.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.infinum.jsonapix.processor.generation.CompileClasspath
import com.squareup.kotlinpoet.ClassName

/** Looks classes up through KSP's resolver, which only finds classes on the compile classpath. */
internal class KspCompileClasspath(
    private val resolver: Resolver,
) : CompileClasspath {

    override fun hasClass(className: ClassName): Boolean =
        resolver.getClassDeclarationByName(className.canonicalName) != null
}
