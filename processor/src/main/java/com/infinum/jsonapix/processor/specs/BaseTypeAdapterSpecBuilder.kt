package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

public abstract class BaseTypeAdapterSpecBuilder {

    public abstract fun getAdapterPrefixName(): String
    public abstract fun getClassSuffixName(): String

    public abstract fun getRootModel(className: ClassName): TypeName

    public abstract fun convertFromStringFunSpec(
        className: ClassName,
        modelType: TypeName,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?,
    ): FunSpec

    public abstract fun getAdditionalImports(): List<String>

    public fun build(
        className: ClassName,
        rootLinks: String?,
        resourceObjectLinks: String?,
        relationshipsLinks: String?,
        rootMeta: ClassName?,
        resourceObjectMeta: ClassName?,
        relationshipsMeta: ClassName?,
        errors: String?
    ): FileSpec {
        val generatedName = getAdapterPrefixName().withName(className.simpleName)
        val typeAdapterClassName = ClassName(
            className.packageName,
            generatedName
        )
        val modelType = getWrapperClassName(className)
        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(typeAdapterClassName)
                    .addSuperinterface(TypeAdapter::class.asClassName().parameterizedBy(modelType))
                    .addFunction(convertToStringFunSpec(className, modelType))
                    .addFunction(convertFromStringFunSpec(className, modelType, rootMeta, resourceObjectMeta, relationshipsMeta))
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

                        if (rootMeta != null) {
                            addFunction(metaFunSpec(JsonApiConstants.Members.ROOT_META, rootMeta.canonicalName))
                        }
                        if (resourceObjectMeta != null) {
                            addFunction(
                                metaFunSpec(JsonApiConstants.Members.RESOURCE_OBJECT_META, resourceObjectMeta.canonicalName)
                            )
                        }
                        if (relationshipsMeta != null) {
                            addFunction(metaFunSpec(JsonApiConstants.Members.RELATIONSHIPS_META, relationshipsMeta.canonicalName))
                        }

                        if (errors != null) {
                            addFunction(errorsFunSpec(errors))
                        }
                    }
                    .build()
            )
            .addImport(
                JsonApiConstants.Packages.JSONX,
                getAdditionalImports(),
            )
            .build()
    }

    private fun convertToStringFunSpec(className: ClassName, modelType: TypeName): FunSpec {
        return FunSpec.builder(JsonApiConstants.Members.CONVERT_TO_STRING)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("input", modelType)
            .returns(String::class)
            .addStatement(
                """return ""  """
//                "return input.%N(%N(), %N(), %N(), %N(), %N(), %N(), %N())",
//                JsonApiConstants.Members.JSONX_SERIALIZE,
//                JsonApiConstants.Members.ROOT_LINKS,
//                JsonApiConstants.Members.RESOURCE_OBJECT_LINKS,
//                JsonApiConstants.Members.RELATIONSHIPS_LINKS,
//                JsonApiConstants.Members.ROOT_META,
//                JsonApiConstants.Members.RESOURCE_OBJECT_META,
//                JsonApiConstants.Members.RELATIONSHIPS_META,
//                JsonApiConstants.Keys.ERRORS
            )
            .build()
    }

    private fun getWrapperClassName(rootType: ClassName): TypeName =
        ClassName.bestGuess(rootType.canonicalName.withName(getClassSuffixName()))

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

    private fun metaFunSpec(methodName: String, meta: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %S", meta)
            .build()
    }
}
