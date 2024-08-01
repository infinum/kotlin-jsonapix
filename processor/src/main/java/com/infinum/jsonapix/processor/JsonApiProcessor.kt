package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.annotations.JsonApiXErrorList
import com.infinum.jsonapix.annotations.JsonApiXLinks
import com.infinum.jsonapix.annotations.JsonApiXLinksList
import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.annotations.JsonApiXMetaList
import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValues
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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toKmClass
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SuppressWarnings("SpreadOperator")
public class JsonApiProcessor : AbstractProcessor() {

    private val collector = JsonXExtensionsSpecBuilder()
    private val adapterFactoryCollector = TypeAdapterFactorySpecBuilder()
    private val customLinks = mutableListOf<LinksInfo>()
    private val customMetas = mutableListOf<MetaInfo>()
    private val customErrors = mutableMapOf<String, ClassName>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(
            JsonApiX::class.java.name,
            JsonApiXLinks::class.java.name,
            JsonApiXMeta::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        roundEnv?.processLinksAnnotation()
        roundEnv?.processMetaAnnotations()
        roundEnv?.processErrorAnnotation()

        val elements = roundEnv?.getElementsAnnotatedWith(JsonApiX::class.java)
        // process method might get called multiple times and not finding elements is a possibility
        if (elements?.isNullOrEmpty() == false) {
            elements.forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes can be annotated"
                    )
                    return true
                }

                val type = it.getAnnotationParameterValue<JsonApiX, String> { type }
                val isNullable = it.getAnnotationParameterValue<JsonApiX, Boolean> { isNullable }
                processAnnotation(it, type, isNullable)
            }

            val kaptKotlinGeneratedDir =
                processingEnv.options[JsonApiConstants.KAPT_KOTLIN_GENERATED_OPTION_NAME]
            collector.build().writeTo(File(kaptKotlinGeneratedDir!!))
            adapterFactoryCollector.build().writeTo(File(kaptKotlinGeneratedDir))
        }
        return true
    }

    @SuppressWarnings("LongMethod")
    private fun processAnnotation(element: Element, type: String, isNullable: Boolean) {
        val className = element.simpleName.toString()
        val generatedPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val kaptKotlinGeneratedDir =
            processingEnv.options[JsonApiConstants.KAPT_KOTLIN_GENERATED_OPTION_NAME]

        val metadata = element.getAnnotation(Metadata::class.java)
        val typeSpec = metadata.toKmClass().toTypeSpec(
            ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
        )

        val inputDataClass = ClassName(generatedPackage, className)
        val generatedJsonWrapperName = JsonApiConstants.Prefix.JSON_API_X.withName(className)
        val generatedJsonWrapperListName =
            JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className)
        val generatedResourceObjectName =
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className)

        val jsonWrapperClassName = ClassName(generatedPackage, generatedJsonWrapperName)
        val jsonWrapperListClassName = ClassName(generatedPackage, generatedJsonWrapperListName)
        val resourceObjectClassName = ClassName(generatedPackage, generatedResourceObjectName)

        var attributesClassName: ClassName? = null
        var relationshipsClassName: ClassName? = null
        val membersSeparator = PropertyTypesSeparator(typeSpec)
        val primitives = membersSeparator.getPrimitiveProperties()

        if (primitives.isNotEmpty()) {
            val attributesTypeSpec =
                AttributesSpecBuilder.build(
                    ClassName(generatedPackage, className),
                    primitives,
                    type
                )
            val attributesFileSpec = FileSpec.builder(generatedPackage, attributesTypeSpec.name!!)
                .addType(attributesTypeSpec).build()

            attributesFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedAttributesObjectName =
                JsonApiConstants.Prefix.ATTRIBUTES.withName(className)
            attributesClassName =
                ClassName(generatedPackage, generatedAttributesObjectName)
        }

        val oneRelationships = membersSeparator.getOneRelationships()
        val manyRelationships = membersSeparator.getManyRelationships()

        if (oneRelationships.isNotEmpty() || manyRelationships.isNotEmpty()) {
            val relationshipsTypeSpec = RelationshipsSpecBuilder.build(
                inputDataClass,
                type,
                oneRelationships,
                manyRelationships
            )

            val relationshipsFileSpec =
                FileSpec.builder(generatedPackage, relationshipsTypeSpec.name!!)
                    .addType(relationshipsTypeSpec)
                    .addImport(
                        JsonApiConstants.Packages.CORE,
                        JsonApiConstants.Imports.JSON_API_MODEL
                    )
                    .addImport(
                        JsonApiConstants.Packages.JSONX,
                        *JsonApiConstants.Imports.RELATIONSHIP_EXTENSIONS
                    )
                    .build()
            relationshipsFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedRelationshipsObjectName =
                JsonApiConstants.Prefix.RELATIONSHIPS.withName(className)
            relationshipsClassName = ClassName(generatedPackage, generatedRelationshipsObjectName)
        }

        val metaInfo = customMetas.firstOrNull { it.type == type }
        val linksInfo = customLinks.firstOrNull { it.type == type }
        val customError = customErrors[type]

        collector.add(
            type = type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            isNullable = isNullable,
            data = inputDataClass,
            wrapper = jsonWrapperClassName,
            wrapperList = jsonWrapperListClassName,
            resourceObject = resourceObjectClassName,
            attributesObject = attributesClassName,
            relationshipsObject = relationshipsClassName,
            includedStatement = IncludedSpecBuilder.build(
                oneRelationships,
                manyRelationships
            ),
            includedListStatement = IncludedSpecBuilder.buildForList(
                oneRelationships,
                manyRelationships
            )
        )

        adapterFactoryCollector.add(inputDataClass)

        val resourceFileSpec =
            ResourceObjectSpecBuilder.build(
                className = inputDataClass,
                metaClassName = metaInfo?.resourceObjectClassName,
                linksInfo = linksInfo,
                type = type,
                attributes = primitives,
                oneRelationships = mapOf(*oneRelationships.map { it.name to it.type }.toTypedArray()),
                manyRelationships = mapOf(*manyRelationships.map { it.name to it.type }.toTypedArray())
            )

        val wrapperFileSpec = JsonApiXSpecBuilder.build(inputDataClass, isNullable, type, metaInfo, linksInfo, customError)
        val wrapperListFileSpec = JsonApiXListSpecBuilder.build(inputDataClass, isNullable, type, metaInfo, linksInfo, customError)
        val modelFileSpec = JsonApiModelSpecBuilder.build(inputDataClass, isNullable, metaInfo, linksInfo, customError)
        val listItemFileSpec = JsonApiListItemSpecBuilder.build(inputDataClass, isNullable, metaInfo, linksInfo, customError)
        val listFileSpec = JsonApiListSpecBuilder.build(inputDataClass, isNullable, metaInfo, linksInfo, customError)

        val typeAdapterFileSpec = TypeAdapterSpecBuilder.build(
            className = inputDataClass,
            rootLinks = linksInfo?.rootLinks,
            resourceObjectLinks = linksInfo?.resourceObjectLinks,
            relationshipsLinks = linksInfo?.relationshipsLinks,
            rootMeta = metaInfo?.rootClassName,
            resourceObjectMeta = metaInfo?.resourceObjectClassName,
            relationshipsMeta = metaInfo?.relationshipsClassNAme,
            errors = customError?.canonicalName
        )

        val typeAdapterListFileSpec = TypeAdapterListSpecBuilder.build(
            className = inputDataClass,
            rootLinks = linksInfo?.rootLinks,
            resourceObjectLinks = linksInfo?.resourceObjectLinks,
            relationshipsLinks = linksInfo?.relationshipsLinks,
            rootMeta = metaInfo?.rootClassName,
            resourceObjectMeta = metaInfo?.resourceObjectClassName,
            relationshipsMeta = metaInfo?.relationshipsClassNAme,
            errors = customError?.canonicalName
        )

        resourceFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))
        wrapperFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        wrapperListFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        typeAdapterFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        typeAdapterListFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        modelFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        listItemFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        listFileSpec.writeTo(File(kaptKotlinGeneratedDir))
    }

    private fun RoundEnvironment.processErrorAnnotation() {
        val repeatedAnnotations = this.getElementsAnnotatedWith(JsonApiXErrorList::class.java)
        val singleAnnotations = this.getElementsAnnotatedWith(JsonApiXError::class.java)

        repeatedAnnotations?.forEach { element ->
            val types = element.getAnnotationParameterValues<JsonApiXError, List<String>> {
                this.map { annotation -> annotation.type }
            }
            types.forEach { type ->
                storeCustomError(element = element, type = type)
            }
        }

        singleAnnotations?.forEach { element ->
            val type = element.getAnnotationParameterValue<JsonApiXError, String> { type }
            storeCustomError(element = element, type = type)
        }

        collector.addCustomErrors(customErrors)
    }

    private fun storeCustomError(element: Element, type: String) {
        val className = ClassName(
            processingEnv.elementUtils.getPackageOf(element).toString(),
            element.simpleName.toString()
        )
        customErrors[type] = className
    }

    private fun RoundEnvironment.processLinksAnnotation() {
        val repeatedAnnotations = this.getElementsAnnotatedWith(JsonApiXLinksList::class.java)
        val singleAnnotations = this.getElementsAnnotatedWith(JsonApiXLinks::class.java)

        val classNamesFromRepeatedAnnotations = repeatedAnnotations?.flatMap { element ->
            val typeAndPlacementStrategyList =
                element.getAnnotationParameterValues<JsonApiXLinks, List<Pair<String, LinksPlacementStrategy>>> {
                    this.map { annotation -> annotation.type to annotation.placementStrategy }
                }

            typeAndPlacementStrategyList.map { typeAndPlacementStrategy ->
                storeCustomLinksAndReturnClassName(
                    element = element,
                    type = typeAndPlacementStrategy.first,
                    placementStrategy = typeAndPlacementStrategy.second
                )
            }
        }

        val classNamesFromSingleAnnotations = singleAnnotations?.map { element ->
            val (type, placementStrategy) =
                element.getAnnotationParameterValue<JsonApiXLinks, Pair<String, LinksPlacementStrategy>> { type to placementStrategy }

            storeCustomLinksAndReturnClassName(element = element, type = type, placementStrategy = placementStrategy)
        }

        collector.addCustomLinks(links = classNamesFromRepeatedAnnotations.orEmpty() + classNamesFromSingleAnnotations.orEmpty())
    }

    private fun storeCustomLinksAndReturnClassName(element: Element, type: String, placementStrategy: LinksPlacementStrategy): ClassName {
        val className = ClassName(
            processingEnv.elementUtils.getPackageOf(element).toString(),
            element.simpleName.toString()
        )
        customLinks.firstOrNull { linksInfo -> linksInfo.type == type }?.let { linksInfo ->
            when (placementStrategy) {
                LinksPlacementStrategy.ROOT -> linksInfo.rootLinks = className
                LinksPlacementStrategy.DATA -> linksInfo.resourceObjectLinks = className
                LinksPlacementStrategy.RELATIONSHIPS -> linksInfo.relationshipsLinks = className
            }
        } ?: kotlin.run {
            val linksInfo = LinksInfo(type)
            when (placementStrategy) {
                LinksPlacementStrategy.ROOT -> linksInfo.rootLinks = className
                LinksPlacementStrategy.DATA -> linksInfo.resourceObjectLinks = className
                LinksPlacementStrategy.RELATIONSHIPS -> linksInfo.relationshipsLinks = className
            }
            customLinks.add(linksInfo)
        }
        return className
    }

    private fun RoundEnvironment.processMetaAnnotations() {
        val repeatedAnnotations = this.getElementsAnnotatedWith(JsonApiXMetaList::class.java)
        val singleAnnotations = this.getElementsAnnotatedWith(JsonApiXMeta::class.java)

        val classNamesFromRepeatedAnnotations = repeatedAnnotations?.flatMap { element ->
            val typeAndPlacementStrategyList =
                element.getAnnotationParameterValues<JsonApiXMeta, List<Pair<String, MetaPlacementStrategy>>> {
                    this.map { annotation -> annotation.type to annotation.placementStrategy }
                }

            typeAndPlacementStrategyList.map { typeAndPlacementStrategy ->
                storeCustomMetaAndReturnClassName(
                    element = element,
                    type = typeAndPlacementStrategy.first,
                    placementStrategy = typeAndPlacementStrategy.second
                )
            }
        }

        val classNamesFromSingleAnnotations = singleAnnotations?.map { element ->
            val (type, placementStrategy) =
                element.getAnnotationParameterValue<JsonApiXMeta, Pair<String, MetaPlacementStrategy>> { type to placementStrategy }

            storeCustomMetaAndReturnClassName(element = element, type = type, placementStrategy = placementStrategy)
        }

        collector.addCustomMetas(meta = classNamesFromRepeatedAnnotations.orEmpty() + classNamesFromSingleAnnotations.orEmpty())
    }

    private fun storeCustomMetaAndReturnClassName(element: Element, type: String, placementStrategy: MetaPlacementStrategy): ClassName {
        val className = ClassName(
            processingEnv.elementUtils.getPackageOf(element).toString(),
            element.simpleName.toString()
        )
        customMetas.firstOrNull { metaInfo -> metaInfo.type == type }?.let { metaInfo ->
            when (placementStrategy) {
                MetaPlacementStrategy.ROOT -> metaInfo.rootClassName = className
                MetaPlacementStrategy.DATA -> metaInfo.resourceObjectClassName = className
                MetaPlacementStrategy.RELATIONSHIPS -> metaInfo.relationshipsClassNAme = className
            }
        } ?: kotlin.run {
            val metaInfo = MetaInfo(type)
            when (placementStrategy) {
                MetaPlacementStrategy.ROOT -> metaInfo.rootClassName = className
                MetaPlacementStrategy.DATA -> metaInfo.resourceObjectClassName = className
                MetaPlacementStrategy.RELATIONSHIPS -> metaInfo.relationshipsClassNAme = className
            }
            customMetas.add(metaInfo)
        }
        return className
    }
}
