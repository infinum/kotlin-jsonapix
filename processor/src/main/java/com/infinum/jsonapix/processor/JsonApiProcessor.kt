package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.specs.AttributesModelSpecBuilder
import com.infinum.jsonapix.processor.specs.IncludedModelSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiWrapperSpecBuilder
import com.infinum.jsonapix.processor.specs.RelationshipModelSpecBuilder
import com.infinum.jsonapix.processor.specs.ResourceIdentifierSpecBuilder
import com.infinum.jsonapix.processor.specs.ResourceObjectSpecBuilder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class JsonApiProcessor : AbstractProcessor() {

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private const val WRAPPER_NAME_PREFIX = "JsonApiSerializable_"
        private const val RESOURCE_OBJECT_PREFIX = "ResourceObject_"
        private const val ATTRIBUTES_OBJECT_PREFIX = "AttributesModel_"
        private const val RELATIONSHIPS_OBJECT_PREFIX = "RelationshipsModel_"
        private const val INCLUDED_OBJECT_PREFIX = "IncludedModel_"
    }

    private val collector = JsonApiExtensionsSpecBuilder()

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(JsonApiX::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    @KotlinPoetMetadataPreview
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
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

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            collector.build().writeTo(File(kaptKotlinGeneratedDir!!))
        }
        return true
    }

    @KotlinPoetMetadataPreview
    private fun processAnnotation(element: Element, type: String) {
        val className = element.simpleName.toString()
        val generatedPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        val metadata = element.getAnnotation(Metadata::class.java)
        val typeSpec = metadata.toImmutableKmClass().toTypeSpec(
            ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
        )

        val inputDataClass = ClassName(generatedPackage, className)
        val generatedJsonWrapperName = "$WRAPPER_NAME_PREFIX$className"
        val generatedResourceObjectName = "$RESOURCE_OBJECT_PREFIX$className"

        val jsonWrapperClassName = ClassName(generatedPackage, generatedJsonWrapperName)
        val resourceObjectClassName = ClassName(generatedPackage, generatedResourceObjectName)

        var hasPrimitives = false
        var hasComposites = false
        var attributesClassName: ClassName? = null
        var includedClassName: ClassName? = null
        var relationshipsClassName: ClassName? = null
        val membersSeparator = PropertyTypesSeparator(typeSpec)
        val primitives = membersSeparator.getPrimitiveProperties()
        val composites = membersSeparator.getCompositeProperties()

        if (primitives.isNotEmpty()) {
            hasPrimitives = true
            val attributesTypeSpec =
                AttributesModelSpecBuilder.build(
                    primitives,
                    ClassName(generatedPackage, className),
                    type
                )
            val attributesFileSpec = FileSpec.builder(generatedPackage, attributesTypeSpec.name!!)
                .addType(attributesTypeSpec).build()

            attributesFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedAttributesObjectName = "$ATTRIBUTES_OBJECT_PREFIX$className"
            attributesClassName =
                ClassName(generatedPackage, generatedAttributesObjectName)
        }

        if (composites.isNotEmpty()) {
            hasComposites = true
            val includedTypeSpec =
                IncludedModelSpecBuilder.build(
                    composites,
                    ClassName(generatedPackage, className),
                    type
                )
            val includedFileSpec = FileSpec.builder(generatedPackage, includedTypeSpec.name!!)
                .addType(includedTypeSpec).build()

            includedFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedIncludedObjectName = "$INCLUDED_OBJECT_PREFIX$className"
            includedClassName = ClassName(generatedPackage, generatedIncludedObjectName)
        }

        val oneRelationships = membersSeparator.getOneRelationships()
        val manyRelationships = membersSeparator.getManyRelationships()

        if (oneRelationships.isNotEmpty() || manyRelationships.isNotEmpty()) {
            val relationshipsTypeSpec = RelationshipModelSpecBuilder.build(
                inputDataClass,
                type,
                oneRelationships,
                manyRelationships
            )

            val relationshipsFileSpec = FileSpec.builder(generatedPackage, relationshipsTypeSpec.name!!)
                .addType(relationshipsTypeSpec)
                .build()
            relationshipsFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))

            val generatedRelationshipsObjectName = "$RELATIONSHIPS_OBJECT_PREFIX$className"
            relationshipsClassName = ClassName(generatedPackage, generatedRelationshipsObjectName)
        }

        collector.add(
            type,
            inputDataClass,
            jsonWrapperClassName,
            resourceObjectClassName,
            attributesClassName,
            relationshipsClassName,
            includedClassName
        )

        val resourceFileSpec =
            ResourceObjectSpecBuilder.build(
                generatedPackage,
                className,
                type,
                hasPrimitives,
                hasComposites
            )
        val wrapperFileSpec = JsonApiWrapperSpecBuilder.build(generatedPackage, className, type)
        val resourceIdentifierFileSpec = ResourceIdentifierSpecBuilder
            .build(generatedPackage, className, type)

        resourceIdentifierFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))
        resourceFileSpec.writeTo(File(kaptKotlinGeneratedDir))
        wrapperFileSpec.writeTo(File(kaptKotlinGeneratedDir))
    }
}
