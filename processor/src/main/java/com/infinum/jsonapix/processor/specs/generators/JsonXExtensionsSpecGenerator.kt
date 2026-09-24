package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.extensions.toLinksInfo
import com.infinum.jsonapix.processor.extensions.toMetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXCoreExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXModelExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXSerializerModuleSpecBuilder
import com.infinum.jsonapix.processor.specs.models.ClassInfo
import com.infinum.jsonapix.processor.specs.specbuilders.IncludedSpecBuilder
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal class JsonXExtensionsSpecGenerator(
    private val holders: Set<JsonApiXHolder>,
    private val linksHolders: Set<JsonApiXLinksHolder>,
    private val metaHolders: Set<JsonApiXMetaHolder>,
    private val errorHolders: Set<JsonApiXErrorHolder>,
) : SpecGenerator {

    override fun generate(outputDir: File) {
        val specsMap = hashMapOf<ClassName, ClassInfo>()

        // 1. Write one extensions file per holder
        holders.forEach { holder ->
            val classInfo = buildClassInfo(holder)
            specsMap[holder.className] = classInfo
            JsonXModelExtensionsSpecBuilder.build(holder.className, classInfo).writeTo(outputDir)
        }

        // 2. Write the serializer module file (with format property)
        JsonXSerializerModuleSpecBuilder.build(
            specsMap = specsMap,
            customLinks = linksHolders.map { it.className },
            customErrors = errorHolders.associate { it.type to it.className },
            customMeta = metaHolders.map { it.className },
        ).writeTo(outputDir)

        // 3. Write the core extensions file
        JsonXCoreExtensionsSpecBuilder.build().writeTo(outputDir)
    }

    private fun buildClassInfo(holder: JsonApiXHolder): ClassInfo {
        val metaInfo = metaHolders.toMetaInfo(holder.type)
        val linksInfo = linksHolders.toLinksInfo(holder.type)

        val generatedPackage = holder.className.packageName
        val className = holder.className

        val jsonWrapperClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName),
        )
        val jsonWrapperListClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className.simpleName),
        )
        val resourceObjectClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName),
        )

        val attributesClassName = if (holder.primitiveProperties.isNotEmpty()) {
            ClassName(
                generatedPackage,
                JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName),
            )
        } else {
            null
        }

        val relationshipsClassName =
            if (holder.oneRelationships.isNotEmpty() || holder.manyRelationships.isNotEmpty()) {
                ClassName(
                    generatedPackage,
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName),
                )
            } else {
                null
            }

        return ClassInfo(
            type = holder.type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            isNullable = holder.isNullable,
            jsonWrapperClassName = jsonWrapperClassName,
            jsonWrapperListClassName = jsonWrapperListClassName,
            resourceObjectClassName = resourceObjectClassName,
            attributesWrapperClassName = attributesClassName,
            relationshipsObjectClassName = relationshipsClassName,
            includedStatement = IncludedSpecBuilder.build(holder.oneRelationships, holder.manyRelationships),
            includedListStatement = IncludedSpecBuilder.buildForList(holder.oneRelationships, holder.manyRelationships),
        )
    }
}
