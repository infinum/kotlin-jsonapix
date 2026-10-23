package com.infinum.jsonapix.processor.ksp.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

/**
 * The class name holders use for a model. Like the KAPT collector, it is built from the package and
 * the simple name, rather than from the full nesting of the declaration.
 */
internal fun KSClassDeclaration.modelClassName(): ClassName =
    ClassName(packageName.asString(), simpleName.asString())
