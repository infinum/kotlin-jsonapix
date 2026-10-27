package com.infinum.jsonapix.processor.ksp.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

/**
 * The class name holders use for a declaration. Like the KAPT collectors, it is built from the package
 * and the simple name, rather than from the full nesting of the declaration.
 */
internal fun KSClassDeclaration.holderClassName(): ClassName =
    ClassName(packageName.asString(), simpleName.asString())
