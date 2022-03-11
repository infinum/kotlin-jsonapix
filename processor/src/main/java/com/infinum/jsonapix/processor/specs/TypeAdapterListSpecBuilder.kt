package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

public object TypeAdapterListSpecBuilder {

    public fun build(
        className: ClassName,
        rootLinks: String?,
        resourceObjectLinks: String?,
        relationshipsLinks: String?
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.TYPE_ADAPTER_LIST.withName(className.simpleName)
        val typeAdapterClassName = ClassName(
            className.packageName,
            generatedName
        )

        val listType = Iterable::class.asClassName().parameterizedBy(className)
        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(typeAdapterClassName)
                    .addSuperinterface(TypeAdapter::class.asClassName().parameterizedBy(listType))
                    .addFunction(convertToStringFunSpec(className))
                    .addFunction(convertFromStringFunSpec(className))
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
                    .build()
            )
            .addImport(
                JsonApiConstants.Packages.JSONX,
                JsonApiConstants.Members.JSONX_SERIALIZE,
                JsonApiConstants.Members.JSONX_LIST_DESERIALIZE
            )
            .build()
    }

    private fun convertToStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_TO_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", Iterable::class.asClassName().parameterizedBy(className))
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
            .returns(Iterable::class.asClassName().parameterizedBy(className))
            .addStatement("val data = input.${JsonApiConstants.Members.JSONX_LIST_DESERIALIZE}<%T>(rootLinks(), resourceObjectLinks(), relationshipsLinks())", className)
            .addStatement("val original = data.${JsonApiConstants.Members.ORIGINAL}")
            .addStatement("data.data?.let { resourceData ->")
            .addStatement("original.zip(resourceData) { model, resource ->")
            .addStatement("(model as? %T)?.let { safeModel ->", JsonApiModel::class)
            .addStatement("safeModel.setRootLinks(data.links)")
            .addStatement("safeModel.setResourceLinks(resource.links)")
            .addStatement("resource.relationshipsLinks()?.let { relationshipLinks -> safeModel.setRelationshipsLinks(relationshipLinks)}")
            .addStatement("}")
            .addStatement("}")
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
}
