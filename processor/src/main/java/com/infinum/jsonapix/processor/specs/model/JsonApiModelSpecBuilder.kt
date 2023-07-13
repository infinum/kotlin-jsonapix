package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName

internal object JsonApiModelSpecBuilder : BaseJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_MODEL
    override fun getRootClassName(rootType: ClassName): ClassName = rootType
    override fun getAdditionalParams(): List<ParameterSpec> {
        return listOf(
            JsonApiConstants.Keys.TYPE.asParam(String::class.asClassName(), true),
            JsonApiConstants.Keys.ID.asParam(String::class.asClassName(), true),
        )
    }
}