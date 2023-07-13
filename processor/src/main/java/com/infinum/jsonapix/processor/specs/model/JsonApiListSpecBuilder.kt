package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

internal object JsonApiListSpecBuilder : BaseJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_LIST
    override fun getRootClassName(rootType: ClassName): TypeName = List::class.asClassName().parameterizedBy(rootType)
    override fun getAdditionalParams(): List<ParameterSpec>  = emptyList()
}