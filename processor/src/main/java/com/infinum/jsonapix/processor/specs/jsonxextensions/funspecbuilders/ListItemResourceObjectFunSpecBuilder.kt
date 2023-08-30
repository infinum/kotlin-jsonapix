package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

internal object ListItemResourceObjectFunSpecBuilder : BaseResourceObjectFunSpecBuilder() {

    override fun getClassSuffix(): String = JsonApiConstants.Suffix.JSON_API_LIST_ITEM
}
