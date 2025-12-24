package com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import kotlinx.serialization.json.Json

internal object FormatPropertySpecBuilder {

    fun build(): PropertySpec {
        val formatCodeBuilder = CodeBlock.builder()
            .addStatement("%T {", Json::class)
            .indent()
            .addStatement(JsonApiConstants.Statements.ENCODE_DEFAULTS)
            .addStatement(
                JsonApiConstants.Statements.CLASS_DISCRIMINATOR_FORMAT,
                JsonApiConstants.CLASS_DISCRIMINATOR_KEY,
            )
            .addStatement(
                JsonApiConstants.Statements.SERIALIZERS_MODULE_FORMAT,
                JsonApiConstants.Members.JSONX_SERIALIZER_MODULE,
            )
            .addStatement(JsonApiConstants.Statements.IGNORE_UNKNOWN_KEYS)
            .unindent()
            .addStatement("}")
        return PropertySpec.builder(JsonApiConstants.Members.FORMAT, Json::class)
            .initializer(formatCodeBuilder.build())
            .build()
    }
}
