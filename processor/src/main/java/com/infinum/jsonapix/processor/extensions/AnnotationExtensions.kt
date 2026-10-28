package com.infinum.jsonapix.processor.extensions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.tag
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

internal fun AnnotationSpec.getStringParameterValue(name: String): String {
    val annotationMirror = checkNotNull(tag<AnnotationMirror>()) { "$typeName wasn't read from an AnnotationMirror" }
    return annotationMirror.elementValues.entries
        .first { (element, _) -> element.simpleName.contentEquals(name) }
        .value.value as String
}

public inline fun <reified T : Annotation, reified R : Any> Element.getAnnotationParameterValue(
    f: T.() -> R,
): R = getAnnotation(T::class.java).f()

public inline fun <reified T : Annotation, reified R : Any> Element.getAnnotationParameterValues(
    f: Array<T>.() -> R,
): R = getAnnotationsByType(T::class.java).f()

@SuppressWarnings("TooGenericExceptionThrown")
public inline fun <reified T : Annotation> Element.getAnnotationClassValue(
    f: T.() -> KClass<*>,
): TypeMirror = try {
    getAnnotation(T::class.java).f()
    throw Exception("Expected to get a MirroredTypeException")
} catch (e: MirroredTypeException) {
    e.typeMirror
}
