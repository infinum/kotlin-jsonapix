package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public object TypeAdapterSpecBuilder : BaseTypeAdapterSpecBuilder() {
    override fun getAdapterPrefixName(): String = JsonApiConstants.Prefix.TYPE_ADAPTER
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_MODEL

    override fun getRootModel(className: ClassName): TypeName = className
}