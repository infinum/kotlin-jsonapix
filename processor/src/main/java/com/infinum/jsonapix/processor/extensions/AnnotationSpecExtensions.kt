package com.infinum.jsonapix.processor.extensions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName

public fun List<AnnotationSpec>.missingTypeName(typeName: TypeName): Boolean =
    indexOfFirst { it.typeName == typeName } < 0

public fun List<AnnotationSpec>.findAnnotationWithTypeName(typeName: TypeName): AnnotationSpec? =
    this.firstOrNull { annotationSpec ->
        annotationSpec.typeName == typeName
    }