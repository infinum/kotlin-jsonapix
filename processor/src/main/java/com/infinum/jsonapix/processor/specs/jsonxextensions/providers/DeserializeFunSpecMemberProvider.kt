package com.infinum.jsonapix.processor.specs.jsonxextensions.providers

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.MemberName

internal object DeserializeFunSpecMemberProvider {

    val decodeMember = MemberName(
        JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
        JsonApiConstants.Members.DECODE_FROM_STRING
    )
    val findTypeMember = MemberName(
        JsonApiConstants.Packages.TYPE_EXTRACTOR,
        JsonApiConstants.Members.FIND_TYPE
    )
    val jsonObjectMember = MemberName(
        JsonApiConstants.Packages.KOTLINX_SERIALIZATION_JSON,
        JsonApiConstants.Members.JSON_OBJECT
    )
}
