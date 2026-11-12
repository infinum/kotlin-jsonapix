package com.infinum.jsonapix.processor.ksp.extensions

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal fun KSAnnotation.isOfType(annotationClass: KClass<out Annotation>): Boolean =
    // Comparing the short name first avoids resolving the type of every other annotation.
    shortName.asString() == annotationClass.simpleName &&
        annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName

/** Returns the value of the argument for [property], falling back to its default value if it isn't set. */
internal inline fun <reified A : Annotation, reified T : Any> KSAnnotation.getArgumentValue(property: KProperty1<A, T>): T =
    getArgumentValueOrDefault(A::class, property.name) as T

/** Returns the value of the enum argument for [property], falling back to its default value if it isn't set. */
internal inline fun <reified A : Annotation, reified E : Enum<E>> KSAnnotation.getEnumArgumentValue(
    property: KProperty1<A, E>,
): E {
    // KSP2 represents an enum entry by its declaration. KSP plans to represent it by its type instead.
    // A default from the annotation class is the entry itself.
    val entry = when (val value = getArgumentValueOrDefault(A::class, property.name)) {
        is E -> return value
        is KSClassDeclaration -> value
        is KSType -> value.declaration
        else -> error("Argument ${property.name} of $shortName isn't an enum entry: $value")
    }
    return enumValueOf(entry.simpleName.asString())
}

/**
 * Returns the value of the argument [name], or its default value if it isn't set.
 *
 * KSP knows an argument's default only when it reads the annotation class from a class file, which is
 * also where KAPT reads it from. In the metadata compilation of a KMP consumer's commonMain, the
 * annotation classes come from klibs, and KSP reports only the arguments that are written. The default
 * then comes from [annotationClass], the copy this processor is built with, which declares the same
 * defaults.
 */
internal fun KSAnnotation.getArgumentValueOrDefault(annotationClass: KClass<out Annotation>, name: String): Any? {
    val argument = arguments.firstOrNull { it.name?.asString() == name }
        ?: defaultArguments.firstOrNull { it.name?.asString() == name }
    return if (argument != null) {
        argument.value
    } else {
        checkNotNull(annotationClass.java.getMethod(name).defaultValue) {
            "Argument $name of $shortName isn't set and has no default value"
        }
    }
}

/**
 * Returns the [annotationClass] annotations on this symbol in declaration order. KSP reports repeated
 * annotations as written, not inside the container annotation the JVM stores them in.
 */
internal fun KSAnnotated.getRepeatableAnnotations(annotationClass: KClass<out Annotation>): List<KSAnnotation> =
    annotations.filter { it.isOfType(annotationClass) }.toList()
