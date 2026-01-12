package com.infinum.jsonapix.processor.specs.jsonxextensions.providers

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.MemberName

internal object SerializeFunSpecMemberProvider {

    val formatMember = MemberName(
        JsonApiConstants.Packages.JSONX,
        JsonApiConstants.Members.FORMAT,
    )
    val encodeMember = MemberName(
        JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
        JsonApiConstants.Members.ENCODE_TO_STRING,
    )
    val jsonApiWrapperMember = MemberName(
        JsonApiConstants.Packages.JSONX,
        JsonApiConstants.Members.JSONX_WRAPPER_GETTER,
    )

    val jsonApiListWrapperMember =
        MemberName(
            JsonApiConstants.Packages.JSONX,
            JsonApiConstants.Members.JSONX_WRAPPER_LIST_GETTER,
        )
}
