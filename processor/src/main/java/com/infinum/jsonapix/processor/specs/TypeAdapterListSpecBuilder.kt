package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterListSpecBuilder : BaseTypeAdapterSpecBuilder() {
    override fun getAdapterPrefixName(): String = JsonApiConstants.Prefix.TYPE_ADAPTER_LIST
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_LIST
    override fun getRootModel(className: ClassName): TypeName =
        List::class.asClassName().parameterizedBy(className)
}
