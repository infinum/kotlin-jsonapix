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
        relationshipsLinks: String?,
        meta: String?,
        errors: String?
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
                    .apply {
                        if (rootLinks != null) {
                            addFunction(linksFunSpec(JsonApiConstants.Members.ROOT_LINKS, rootLinks))
                        }
                        if (resourceObjectLinks != null) {
                            addFunction(
                                linksFunSpec(JsonApiConstants.Members.RESOURCE_OBJECT_LINKS, resourceObjectLinks)
                            )
                        }
                        if (relationshipsLinks != null) {
                            addFunction(linksFunSpec(JsonApiConstants.Members.RELATIONSHIPS_LINKS, relationshipsLinks))
                        }
                        if (meta != null) {
                            addFunction(metaFunSpec(meta))
                        }
                        if(errors != null) {
                            addFunction(errorsFunSpec(errors))
                        }
                    }
                    .build()
            )
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
                "return input.%N(%N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_SERIALIZE,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Keys.META,
                JsonApiConstants.Keys.ERRORS
            )
            .build()
    }

    private fun convertFromStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(className)
            .addStatement(
                "val data = input.%N<%T>(%N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_DESERIALIZE,
                className,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Keys.META,
                JsonApiConstants.Keys.ERRORS
            )
            .addStatement("val original = data.${JsonApiConstants.Members.ORIGINAL}")
            .addStatement("(original as? %T)?.let {", JsonApiModel::class)
            .addStatement("it.setRootLinks(data.links)")
            .addStatement("it.setResourceLinks(data.data?.links)")
            .addStatement("it.setErrors(data.errors)")
            .addStatement("data.data?.relationshipsLinks()?.let { links -> it.setRelationshipsLinks(links) }")
            .addStatement("it.setMeta(data.meta)")
            .addStatement("}")
            .addStatement("return original")
            .build()
    }

    private fun linksFunSpec(methodName: String, links: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", links)
            .build()
    }

    private fun errorsFunSpec(errors: String): FunSpec {
        return FunSpec.builder(JsonApiConstants.Keys.ERRORS)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", errors)
            .build()
    }

    private fun metaFunSpec(meta: String): FunSpec {
        return FunSpec.builder(JsonApiConstants.Keys.META)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", meta)
            .build()
    }
}
