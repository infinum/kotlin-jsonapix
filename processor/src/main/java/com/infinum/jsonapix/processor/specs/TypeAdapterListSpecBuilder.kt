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

public object TypeAdapterListSpecBuilder {

    public fun build(
        className: ClassName,
        rootLinks: String?,
        resourceObjectLinks: String?,
        relationshipsLinks: String?,
        rootMeta: String?,
        resourceObjectMeta: String?,
        relationshipsMeta: String?,
        errors: String?
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
                            addFunction(
                                linksFunSpec(
                                    JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                                    resourceObjectLinks
                                )
                            )
                        }
                        if (relationshipsLinks != null) {
                            addFunction(
                                linksFunSpec(
                                    JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                                    relationshipsLinks
                                )
                            )
                        }

                        if (rootMeta != null) {
                            addFunction(metaFunSpec(JsonApiConstants.Members.ROOT_META, rootMeta))
                        }
                        if (resourceObjectMeta != null) {
                            addFunction(
                                metaFunSpec(
                                    JsonApiConstants.Members.RESOURCE_OBJECT_META,
                                    resourceObjectMeta
                                )
                            )
                        }
                        if (relationshipsMeta != null) {
                            addFunction(
                                metaFunSpec(
                                    JsonApiConstants.Members.RELATIONSHIPS_META,
                                    relationshipsMeta
                                )
                            )
                        }


                        if (errors != null) {
                            addFunction(errorsFunSpec(errors))
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
                "return input.%N(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_SERIALIZE,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS
            )

            .build()
    }

    private fun convertFromStringFunSpec(className: ClassName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_FROM_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", String::class)
            .returns(Iterable::class.asClassName().parameterizedBy(className))
            .addStatement(
                "val data = input.%N<%T>(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
                JsonApiConstants.Members.JSONX_LIST_DESERIALIZE,
                className,
                JsonApiConstants.Members.ROOT_LINKS,
                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
                JsonApiConstants.Members.ROOT_META,
                JsonApiConstants.Members.RESOURCE_OBJECT_META,
                JsonApiConstants.Members.RELATIONSHIPS_META,
                JsonApiConstants.Keys.ERRORS
            )
            .addStatement("val original = data.${JsonApiConstants.Members.ORIGINAL}")
            .addStatement("data.data?.let { resourceData ->")
            .addStatement("original.zip(resourceData) { model, resource ->")
            .addStatement("(model as? %T)?.let { safeModel ->", JsonApiModel::class)
            .addStatement("safeModel.setRootLinks(data.links)")
            .addStatement("safeModel.setResourceLinks(resource.links)")
            .addStatement("resource.relationshipsLinks()?.let {")
            .addStatement("relationshipLinks -> safeModel.setRelationshipsLinks(relationshipLinks)")
            .addStatement("}")
            .addStatement("safeModel.setRootMeta(data.meta)")
            .addStatement("safeModel.setResourceMeta(resource.meta)")
            .addStatement("resource.relationshipsMeta()?.let {")
            .addStatement("relationshipMeta -> safeModel.setRelationshipsMeta(relationshipMeta.filterValues { it != null })")
            .addStatement("}")
            .addStatement("safeModel.setErrors(data.errors)")
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

    private fun metaFunSpec(methodName: String, meta: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", meta)
            .build()
    }

    private fun errorsFunSpec(errors: String): FunSpec {
        return FunSpec.builder(JsonApiConstants.Keys.ERRORS)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", errors)
            .build()
    }
}
