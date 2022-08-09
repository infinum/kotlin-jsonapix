package com.infinum.jsonapix.processor.specs.jsonxextensions

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.ClassInfo
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ManyRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.OneRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.FormatPropertySpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.WrapperSerializerPropertySpecBuilder
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec

@SuppressWarnings("SpreadOperator")
internal class JsonXExtensionsSpecBuilder {

    private val specsMap = hashMapOf<ClassName, ClassInfo>()
    private val customLinks = mutableListOf<ClassName>()
    private val metas = mutableMapOf<String, ClassName>()

    @SuppressWarnings("LongParameterList")
    fun add(
        type: String,
        data: ClassName,
        wrapper: ClassName,
        wrapperList: ClassName,
        resourceObject: ClassName,
        attributesObject: ClassName?,
        relationshipsObject: ClassName?,
        includedStatement: CodeBlock?,
        includedListStatement: CodeBlock?
    ) {
        specsMap[data] = ClassInfo(
            type,
            wrapper,
            wrapperList,
            resourceObject,
            attributesObject,
            relationshipsObject,
            includedStatement,
            includedListStatement
        )
    }

    fun addCustomLinks(links: List<ClassName>) {
        customLinks.clear()
        customLinks.addAll(links)
    }

    fun addCustomMetas(map: Map<String, ClassName>) {
        metas.clear()
        metas.putAll(map)
    }

    @SuppressWarnings("SpreadOperator", "LongMethod")
    fun build(packageName: String): FileSpec {
        val fileSpec = FileSpec.builder(
            packageName,
            JsonApiConstants.FileNames.JSON_X_EXTENSIONS
        )
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class)
                .addMember("%S", JsonApiConstants.FileNames.JSON_X_EXTENSIONS)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build()
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
            *JsonApiConstants.Imports.KOTLINX
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_DISCRIMINATORS,
            *JsonApiConstants.Imports.CORE_EXTENSIONS
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            *JsonApiConstants.Imports.KOTLINX_MODULES
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.JSONX,
            *JsonApiConstants.Imports.JSON_X
        )

        specsMap.entries.forEach {
            fileSpec.addFunction(
                ResourceObjectFunSpecBuilder.build(
                    it.key,
                    it.value.resourceObjectClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName
                )
            )
            fileSpec.addFunction(
                WrapperFunSpecBuilder.build(
                    it.key,
                    it.value.jsonWrapperClassName,
                    it.value.includedStatement?.toString()
                )
            )
            fileSpec.addFunction(
                WrapperListFunSpecBuilder.build(
                    it.key,
                    it.value.jsonWrapperListClassName,
                    it.value.includedListStatement?.toString()
                )
            )
            fileSpec.addFunction(SerializeFunSpecBuilder.build(packageName, it.key))
            fileSpec.addFunction(SerializeListFunSpecBuilder.build(packageName, it.key))
        }

        fileSpec.addProperty(WrapperSerializerPropertySpecBuilder.build(specsMap, customLinks, metas))
        fileSpec.addProperty(FormatPropertySpecBuilder.build())
        fileSpec.addFunction(ManyRelationshipModelFunSpecBuilder.build())
        fileSpec.addFunction(OneRelationshipModelFunSpecBuilder.build())

        fileSpec.addFunction(DeserializeFunSpecBuilder.build(packageName))
        fileSpec.addFunction(DeserializeListFunSpecBuilder.build(packageName))

        return fileSpec.build()
    }
}
