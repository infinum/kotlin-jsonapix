package com.infinum.jsonapix.processor.subprocessors

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.collectors.JsonApiXCollector
import com.infinum.jsonapix.processor.models.HasManyHolder
import com.infinum.jsonapix.processor.models.HasOneHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.specs.AttributesSpecBuilder
import com.infinum.jsonapix.processor.specs.IncludedSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiXListSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiXSpecBuilder
import com.infinum.jsonapix.processor.specs.RelationshipsSpecBuilder
import com.infinum.jsonapix.processor.specs.ResourceObjectSpecBuilder
import com.infinum.jsonapix.processor.specs.TypeAdapterFactorySpecBuilder
import com.infinum.jsonapix.processor.specs.TypeAdapterListSpecBuilder
import com.infinum.jsonapix.processor.specs.TypeAdapterSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.JsonXExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.model.JsonApiListItemSpecBuilder
import com.infinum.jsonapix.processor.specs.model.JsonApiListSpecBuilder
import com.infinum.jsonapix.processor.specs.model.JsonApiModelSpecBuilder
import com.infinum.jsonapix.processor.validators.JsonApiXValidator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import javax.annotation.processing.RoundEnvironment

internal class JsonApiXSubprocessor : CommonSubprocessor() {

    private val extensionsSpec = JsonXExtensionsSpecBuilder()
    private val adapterFactorySpec = TypeAdapterFactorySpecBuilder()

    private var _linksInfoMap: Map<String, LinksInfo> = emptyMap()
    private var _metaInfoMap: Map<String, MetaInfo> = emptyMap()
    private var _customErrors: Map<String, ClassName> = emptyMap()
    private var _customLinksClassNames: List<ClassName> = emptyList()
    private var _customMetaClassNames: List<ClassName> = emptyList()

    fun setLinksInfo(linksInfoMap: Map<String, LinksInfo>, customLinksClassNames: List<ClassName>) {
        _linksInfoMap = linksInfoMap
        _customLinksClassNames = customLinksClassNames
    }

    fun setMetaInfo(metaInfoMap: Map<String, MetaInfo>, customMetaClassNames: List<ClassName>) {
        _metaInfoMap = metaInfoMap
        _customMetaClassNames = customMetaClassNames
    }

    fun setCustomErrors(customErrors: Map<String, ClassName>) {
        _customErrors = customErrors
    }

    override fun process(roundEnvironment: RoundEnvironment) {
        val collector = JsonApiXCollector(
            roundEnvironment = roundEnvironment,
            elementUtils = elementUtils,
            typeUtils = typeUtils,
            linksInfoMap = _linksInfoMap,
            metaInfoMap = _metaInfoMap,
            customErrors = _customErrors
        )
        val validator = JsonApiXValidator(messager)

        val holders = collector.collect()
        val validatedHolders = validator.validate(holders)

        if (validatedHolders.isEmpty()) return

        // Add custom links, meta, and errors to the extensions collector
        extensionsSpec.addCustomLinks(_customLinksClassNames)
        extensionsSpec.addCustomMetas(_customMetaClassNames)
        extensionsSpec.addCustomErrors(_customErrors)

        validatedHolders.forEach { holder ->
            processHolder(holder)
        }

        // Write the combined extension file and adapter factory
        generatedDir?.let { dir ->
            extensionsSpec.build().writeTo(dir)
            adapterFactorySpec.build().writeTo(dir)
        }
    }

    @SuppressWarnings("LongMethod")
    private fun processHolder(holder: JsonApiXHolder) {
        val className = holder.className
        val generatedPackage = className.packageName
        val type = holder.type
        val isNullable = holder.isNullable

        val generatedJsonWrapperName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val generatedJsonWrapperListName = JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className.simpleName)
        val generatedResourceObjectName = JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)

        val jsonWrapperClassName = ClassName(generatedPackage, generatedJsonWrapperName)
        val jsonWrapperListClassName = ClassName(generatedPackage, generatedJsonWrapperListName)
        val resourceObjectClassName = ClassName(generatedPackage, generatedResourceObjectName)

        var attributesClassName: ClassName? = null
        var relationshipsClassName: ClassName? = null

        val primitives = holder.primitiveProperties
        val oneRelationships = holder.oneRelationships
        val manyRelationships = holder.manyRelationships

        // Convert holders to PropertySpecs for compatibility with existing spec builders
        val oneRelationshipSpecs = oneRelationships.map { it.propertySpec }
        val manyRelationshipSpecs = manyRelationships.map { it.propertySpec }

        // Generate Attributes class if needed
        if (primitives.isNotEmpty()) {
            val attributesTypeSpec = AttributesSpecBuilder.build(className, primitives, type)
            val attributesFileSpec = FileSpec.builder(generatedPackage, attributesTypeSpec.name!!)
                .addType(attributesTypeSpec)
                .build()
            attributesFileSpec.writeTo(generatedDir!!)

            val generatedAttributesObjectName = JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
            attributesClassName = ClassName(generatedPackage, generatedAttributesObjectName)
        }

        // Generate Relationships class if needed
        if (oneRelationships.isNotEmpty() || manyRelationships.isNotEmpty()) {
            val relationshipsTypeSpec = RelationshipsSpecBuilder.build(
                className,
                type,
                oneRelationshipSpecs,
                manyRelationshipSpecs
            )

            val relationshipsFileSpec = FileSpec.builder(generatedPackage, relationshipsTypeSpec.name!!)
                .addType(relationshipsTypeSpec)
                .addImport(JsonApiConstants.Packages.CORE, JsonApiConstants.Imports.JSON_API_MODEL)
                .addImport(JsonApiConstants.Packages.JSONX, *JsonApiConstants.Imports.RELATIONSHIP_EXTENSIONS)
                .build()
            relationshipsFileSpec.writeTo(generatedDir!!)

            val generatedRelationshipsObjectName = JsonApiConstants.Prefix.RELATIONSHIPS.withName(className.simpleName)
            relationshipsClassName = ClassName(generatedPackage, generatedRelationshipsObjectName)
        }

        val metaInfo = holder.metaInfo
        val linksInfo = holder.linksInfo
        val customError = holder.customError

        // Add to extensions collector
        extensionsSpec.add(
            type = type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            isNullable = isNullable,
            data = className,
            wrapper = jsonWrapperClassName,
            wrapperList = jsonWrapperListClassName,
            resourceObject = resourceObjectClassName,
            attributesObject = attributesClassName,
            relationshipsObject = relationshipsClassName,
            includedStatement = IncludedSpecBuilder.build(oneRelationshipSpecs, manyRelationshipSpecs),
            includedListStatement = IncludedSpecBuilder.buildForList(oneRelationshipSpecs, manyRelationshipSpecs)
        )

        adapterFactorySpec.add(className)

        // Generate ResourceObject spec
        val resourceFileSpec = ResourceObjectSpecBuilder.build(
            className = className,
            metaClassName = metaInfo?.resourceObjectClassName,
            linksInfo = linksInfo,
            type = type,
            attributes = primitives,
            oneRelationships = mapOf(*oneRelationshipSpecs.map { it.name to it.type }.toTypedArray()),
            manyRelationships = mapOf(*manyRelationshipSpecs.map { it.name to it.type }.toTypedArray())
        )

        // Generate wrapper specs
        val wrapperFileSpec = JsonApiXSpecBuilder.build(
            className, isNullable, type, metaInfo, linksInfo, customError
        )
        val wrapperListFileSpec = JsonApiXListSpecBuilder.build(
            className, isNullable, type, metaInfo, linksInfo, customError
        )

        // Generate model specs
        val modelFileSpec = JsonApiModelSpecBuilder.build(className, isNullable, metaInfo, linksInfo, customError)
        val listItemFileSpec = JsonApiListItemSpecBuilder.build(className, isNullable, metaInfo, linksInfo, customError)
        val listFileSpec = JsonApiListSpecBuilder.build(className, isNullable, metaInfo, linksInfo, customError)

        // Generate type adapter specs
        val typeAdapterFileSpec = TypeAdapterSpecBuilder.build(
            className = className,
            rootLinks = linksInfo?.rootLinks,
            resourceObjectLinks = linksInfo?.resourceObjectLinks,
            relationshipsLinks = linksInfo?.relationshipsLinks,
            rootMeta = metaInfo?.rootClassName,
            resourceObjectMeta = metaInfo?.resourceObjectClassName,
            relationshipsMeta = metaInfo?.relationshipsClassNAme,
            errors = customError?.canonicalName
        )

        val typeAdapterListFileSpec = TypeAdapterListSpecBuilder.build(
            className = className,
            rootLinks = linksInfo?.rootLinks,
            resourceObjectLinks = linksInfo?.resourceObjectLinks,
            relationshipsLinks = linksInfo?.relationshipsLinks,
            rootMeta = metaInfo?.rootClassName,
            resourceObjectMeta = metaInfo?.resourceObjectClassName,
            relationshipsMeta = metaInfo?.relationshipsClassNAme,
            errors = customError?.canonicalName
        )

        // Write all specs
        generatedDir?.let { dir ->
            resourceFileSpec.writeTo(dir)
            wrapperFileSpec.writeTo(dir)
            wrapperListFileSpec.writeTo(dir)
            typeAdapterFileSpec.writeTo(dir)
            typeAdapterListFileSpec.writeTo(dir)
            modelFileSpec.writeTo(dir)
            listItemFileSpec.writeTo(dir)
            listFileSpec.writeTo(dir)
        }
    }
}
