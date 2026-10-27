package com.infinum.jsonapix.processor.ksp.extensions

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
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

/** Returns the value of the enum argument [name], falling back to its default value if it isn't set. */
internal inline fun <reified E : Enum<E>> KSAnnotation.getEnumArgumentValue(name: String): E {
    // KSP2 represents an enum entry by its declaration. KSP plans to represent it by its type instead.
    val entry = when (val value = getArgumentValue<Any>(name)) {
        is KSClassDeclaration -> value
        is KSType -> value.declaration
        else -> error("Argument $name of $shortName isn't an enum entry: $value")
    }
    return enumValueOf(entry.simpleName.asString())
}

/**
 * Returns the [annotationClass] annotations on this symbol in declaration order, including the ones
 * inside a [containerClass] annotation, the container that repeated annotations are stored in.
 */
internal fun KSAnnotated.getRepeatableAnnotations(
    annotationClass: KClass<out Annotation>,
    containerClass: KClass<out Annotation>,
): List<KSAnnotation> =
    annotations.flatMap { annotation ->
        when {
            annotation.isOfType(annotationClass) -> sequenceOf(annotation)
            annotation.isOfType(containerClass) -> annotation.getArgumentValue<List<KSAnnotation>>("value").asSequence()
            else -> emptySequence()
        }
    }.toList()
