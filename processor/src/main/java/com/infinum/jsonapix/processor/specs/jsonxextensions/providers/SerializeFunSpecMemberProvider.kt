package com.infinum.jsonapix.processor.specs.jsonxextensions.providers

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.MemberName

internal object SerializeFunSpecMemberProvider {

    val encodeMember = MemberName(
        JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
        JsonApiConstants.Members.ENCODE_TO_STRING
    )
}
