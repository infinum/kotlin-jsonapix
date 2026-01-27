package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.models.JsonApiXErrorResult
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksResult
import com.infinum.jsonapix.processor.models.JsonApiXMetaResult
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.specbuilders.IncludedSpecBuilder
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal class JsonXExtensionsSpecGenerator(
    private val holders: Set<JsonApiXHolder>,
    private val linksResult: JsonApiXLinksResult,
    private val metaResult: JsonApiXMetaResult,
    private val errorResult: JsonApiXErrorResult
) : SpecGenerator {

    override fun generate(outputDir: File) {
        val builder = JsonXExtensionsSpecBuilder()

        // Setup custom types
        builder.addCustomLinks(linksResult.customLinksClassNames)
        builder.addCustomMetas(metaResult.customMetaClassNames)
        builder.addCustomErrors(errorResult.customErrors)

        // Register each holder
        holders.forEach { holder ->
            registerHolder(builder, holder)
        }

        builder.build().writeTo(outputDir)
    }

    private fun registerHolder(builder: JsonXExtensionsSpecBuilder, holder: JsonApiXHolder) {
        val metaInfo = metaResult.metaInfoMap[holder.type]
        val linksInfo = linksResult.linksInfoMap[holder.type]

        val generatedPackage = holder.className.packageName
        val className = holder.className

        val jsonWrapperClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        )
        val jsonWrapperListClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className.simpleName)
        )
        val resourceObjectClassName = ClassName(
            generatedPackage,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val attributesClassName = if (holder.primitiveProperties.isNotEmpty()) {
            ClassName(
                generatedPackage,
                JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
            )
        } else null

        val relationshipsClassName =
            if (holder.oneRelationships.isNotEmpty() || holder.manyRelationships.isNotEmpty()) {
                ClassName(
                    generatedPackage,
                    JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName)
                )
            } else null

        builder.add(
            type = holder.type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            isNullable = holder.isNullable,
            data = className,
            wrapper = jsonWrapperClassName,
            wrapperList = jsonWrapperListClassName,
            resourceObject = resourceObjectClassName,
            attributesObject = attributesClassName,
            relationshipsObject = relationshipsClassName,
            includedStatement = IncludedSpecBuilder.build(holder.oneRelationships, holder.manyRelationships),
            includedListStatement = IncludedSpecBuilder.buildForList(holder.oneRelationships, holder.manyRelationships)
        )
    }
}
