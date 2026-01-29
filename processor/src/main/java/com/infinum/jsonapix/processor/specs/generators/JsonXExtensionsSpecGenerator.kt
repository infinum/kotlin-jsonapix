package com.infinum.jsonapix.processor.specs.generators

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.extensions.toLinksInfo
import com.infinum.jsonapix.processor.extensions.toMetaInfo
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXExtensionsSpecBuilder
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
        val builder = JsonXExtensionsSpecBuilder()

        // Setup custom types
        builder.addCustomLinks(linksHolders.map { it.className })
        builder.addCustomMetas(metaHolders.map { it.className })
        builder.addCustomErrors(errorHolders.associate { it.type to it.className })

        // Add each holder
        holders.forEach { holder ->
            addHolder(builder, holder)
        }

        builder.build().writeTo(outputDir)
    }

    private fun addHolder(builder: JsonXExtensionsSpecBuilder, holder: JsonApiXHolder) {
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
            includedListStatement = IncludedSpecBuilder.buildForList(holder.oneRelationships, holder.manyRelationships),
        )
    }
}
