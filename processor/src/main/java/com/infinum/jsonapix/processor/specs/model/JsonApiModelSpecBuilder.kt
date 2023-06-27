package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName

internal object JsonApiModelSpecBuilder : BasicJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_MODEL
    override fun getRootClassName(rootType: ClassName): ClassName = rootType
}