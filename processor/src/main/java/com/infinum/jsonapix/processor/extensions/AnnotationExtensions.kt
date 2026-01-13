package com.infinum.jsonapix.processor.extensions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

public inline fun <reified T : Annotation, reified R : Any> Element.getAnnotationParameterValue(f: T.() -> R): R = getAnnotation(
    T::class.java,
).f()

public inline fun <reified T : Annotation, reified R : Any> Element.getAnnotationParameterValues(f: Array<T>.() -> R): R = getAnnotationsByType(
    T::class.java,
).f()

@SuppressWarnings("TooGenericExceptionThrown")
public inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>): TypeMirror =
    try {
        getAnnotation(T::class.java).f()
        throw Exception("Expected to get a MirroredTypeException")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }

public fun List<AnnotationSpec>.missingTypeName(typeName: TypeName): Boolean = indexOfFirst { it.typeName == typeName } < 0

public fun List<AnnotationSpec>.findAnnotationWithTypeName(typeName: TypeName): AnnotationSpec? =
    this.firstOrNull { annotationSpec ->
        annotationSpec.typeName == typeName
    }
