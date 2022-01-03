package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.Links
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.specs.AttributesSpecBuilder
import com.infinum.jsonapix.processor.specs.IncludedSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiXListSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonXExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiXSpecBuilder
import com.infinum.jsonapix.processor.specs.RelationshipsSpecBuilder
import com.infinum.jsonapix.processor.specs.ResourceObjectSpecBuilder
import com.infinum.jsonapix.processor.specs.TypeAdapterFactorySpecBuilder
import com.infinum.jsonapix.processor.specs.TypeAdapterSpecBuilder
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

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(JsonApiX::class.java.name, Links::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val linksElements = roundEnv?.getElementsAnnotatedWith(Links::class.java).orEmpty().map {
            ClassName(processingEnv.elementUtils.getPackageOf(it).toString(), it.simpleName.toString())
        }
        collector.addCustomLinks(linksElements)

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
                processAnnotation(it, type)
            }

            val kaptKotlinGeneratedDir =
                processingEnv.options[JsonApiConstants.KAPT_KOTLIN_GENERATED_OPTION_NAME]
            collector.build().writeTo(File(kaptKotlinGeneratedDir!!))
            adapterFactoryCollector.build().writeTo(File(kaptKotlinGeneratedDir))
        }
        return true
    }

    @SuppressWarnings("LongMethod")
    private fun processAnnotation(element: Element, type: String) {
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
        val generatedJsonWrapperListName = JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className)
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
                    .build()
            relationshipsFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedRelationshipsObjectName =
                JsonApiConstants.Prefix.RELATIONSHIPS.withName(className)
            relationshipsClassName = ClassName(generatedPackage, generatedRelationshipsObjectName)
        }

        collector.add(
            type,
            inputDataClass,
            jsonWrapperClassName,
            jsonWrapperListClassName,
            resourceObjectClassName,
            attributesClassName,
            relationshipsClassName,
            IncludedSpecBuilder.build(
                oneRelationships,
                manyRelationships
            ),
            IncludedSpecBuilder.buildForList(
                oneRelationships,
                manyRelationships
            )
        )

        adapterFactoryCollector.add(inputDataClass)

        val resourceFileSpec =
            ResourceObjectSpecBuilder.build(
                inputDataClass,
                type,
                primitives,
                mapOf(*oneRelationships.map { it.name to it.type }.toTypedArray()),
                mapOf(*manyRelationships.map { it.name to it.type }.toTypedArray())
            )
        val wrapperFileSpec =
            JsonApiXSpecBuilder.build(inputDataClass, type)
        val wrapperListFileSpec =
            JsonApiXListSpecBuilder.build(inputDataClass, type)
        val typeAdapterFileSpec = TypeAdapterSpecBuilder.build(inputDataClass)

        resourceFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))
        wrapperFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        wrapperListFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        typeAdapterFileSpec.writeTo(File(kaptKotlinGeneratedDir))
    }
}
