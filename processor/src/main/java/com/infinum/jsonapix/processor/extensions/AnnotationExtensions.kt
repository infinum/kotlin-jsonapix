package com.infinum.jsonapix.processor.extensions

import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

inline fun <reified T : Annotation, reified R : Any> Element.getAnnotationParameterValue(f: T.() -> R) =
    getAnnotation(T::class.java).f()

@SuppressWarnings("TooGenericExceptionThrown")
inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>): TypeMirror = try {
    getAnnotation(T::class.java).f()
    throw Exception("Expected to get a MirroredTypeException")
} catch (e: MirroredTypeException) {
    e.typeMirror
}
