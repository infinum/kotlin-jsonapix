package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiModel
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

    public fun build(
        className: ClassName,
        rootLinks: String?,
        resourceObjectLinks: String?,
        relationshipsLinks: String?
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.TYPE_ADAPTER.withName(className.simpleName)
        val typeAdapterClassName = ClassName(
            className.packageName,
            generatedName
        )
        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(typeAdapterClassName)
                    .addSuperinterface(TypeAdapter::class.asClassName().parameterizedBy(className))
                    .addFunction(convertToStringFunSpec(className))
                    .addFunction(convertFromStringFunSpec(className))
                    .build()
            )
            .apply {
                if (rootLinks != null) {
                    addFunction(linksFunSpec(JsonApiConstants.Members.ROOT_LINKS, rootLinks))
                }
                if (resourceObjectLinks != null) {
                    addFunction(linksFunSpec(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, resourceObjectLinks))
                }
                if (relationshipsLinks != null) {
                    addFunction(linksFunSpec(JsonApiConstants.Members.RELATIONSHIPS_LINKS, relationshipsLinks))
                }
            }
            .addImport(
                JsonApiConstants.Packages.JSONX,
                JsonApiConstants.Members.JSONX_SERIALIZE,
                JsonApiConstants.Members.JSONX_DESERIALIZE
            )
            .build()
    }

    private fun convertToStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_TO_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", className)
            .returns(String::class)
            .addStatement(
                "return input.${JsonApiConstants.Members.JSONX_SERIALIZE}(${JsonApiConstants.Members.ROOT_LINKS}(), ${JsonApiConstants.Members.RESOURCE_OBJECT_LINKS}(), ${JsonApiConstants.Members.RELATIONSHIPS_LINKS}())"
            )
            .build()
    }

    private fun convertFromStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(className)
            .addStatement("val data = input.${JsonApiConstants.Members.JSONX_DESERIALIZE}<%T>(rootLinks(), resourceObjectLinks(), relationshipsLinks())", className)
            .addStatement("val original = data.${JsonApiConstants.Members.ORIGINAL}")
            .addStatement("(original as? %T)?.let {", JsonApiModel::class)
            .addStatement("it.setRootLinks(data.links)")
            .addStatement("}")
            .addStatement("return original")
            .build()
    }

    private fun linksFunSpec(methodName: String, links: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return $links")
            .build()
    }
}
