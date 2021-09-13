package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.AnnotationSpec
import kotlinx.serialization.SerialName

object Specs {
    fun getSerialNameSpec(name: String): AnnotationSpec =
        AnnotationSpec.builder(SerialName::class)
            .addMember(JsonApiConstants.SERIAL_NAME_FORMAT, name)
            .build()
}