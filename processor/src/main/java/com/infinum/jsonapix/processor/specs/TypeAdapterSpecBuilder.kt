package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterSpecBuilder {

    public fun build(className: ClassName): FileSpec {
        val generatedName = JsonApiConstants.Prefix.TYPE_ADAPTER.withName(className.simpleName)
        val typeAdapterClassName = ClassName(
            className.packageName,
            generatedName
        )
        return FileSpec.builder(className.packageName, generatedName)
            .addType(TypeSpec.classBuilder(typeAdapterClassName)
                .addSuperinterface(TypeAdapter::class.asClassName().parameterizedBy(className))
                .addFunction(convertToStringFunSpec(className))
                .addFunction(convertFromStringFunSpec(className))
                .build())
            .build()
    }

    private fun convertToStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_TO_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", className)
            .returns(String::class)
            .addStatement("return input.${JsonApiConstants.Members.JSONX_SERIALIZE}()")
            .build()
    }

    private fun convertFromStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(className)
            .addStatement("return input.${JsonApiConstants.Members.JSONX_DESERIALIZE}<%T>()", className)
            .build()
    }
}