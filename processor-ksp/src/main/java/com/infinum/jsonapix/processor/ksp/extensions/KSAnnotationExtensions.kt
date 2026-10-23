package com.infinum.jsonapix.processor.ksp.extensions

import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

internal fun KSAnnotation.isOfType(annotationClass: KClass<out Annotation>): Boolean =
    // Comparing the short name first avoids resolving the type of every other annotation.
    shortName.asString() == annotationClass.simpleName &&
        annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName

/** Returns the value of the argument [name], falling back to its default value if it isn't set. */
internal inline fun <reified T : Any> KSAnnotation.getArgumentValue(name: String): T {
    val argument = arguments.firstOrNull { it.name?.asString() == name }
        ?: defaultArguments.first { it.name?.asString() == name }
    return argument.value as T
}
