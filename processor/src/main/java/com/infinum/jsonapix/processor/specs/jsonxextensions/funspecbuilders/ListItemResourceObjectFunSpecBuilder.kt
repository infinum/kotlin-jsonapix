package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants

internal object ListItemResourceObjectFunSpecBuilder : BaseResourceObjectFunSpecBuilder() {

    override fun getClassSuffix(): String = JsonApiConstants.Suffix.JSON_API_LIST_ITEM
}
