package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.specs.JsonApiExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiWrapperSpecBuilder
import com.infinum.jsonapix.processor.specs.ResourceObjectSpecBuilder
import com.squareup.kotlinpoet.ClassName
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
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(JsonApiX::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val collector = JsonApiExtensionsSpecBuilder()
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
                val className = it.simpleName.toString()
                val elementPackage = processingEnv.elementUtils.getPackageOf(it).toString()
                val dataClass = ClassName(elementPackage, className)
                val generatedJsonWrapperName = "$WRAPPER_NAME_PREFIX$className"
                val generatedResourceObjectName = "$RESOURCE_OBJECT_PREFIX$className"
                val jsonWrapperClass = ClassName(elementPackage, generatedJsonWrapperName)
                val resourceObjectClassName = ClassName(elementPackage, generatedResourceObjectName)
                collector.add(dataClass, jsonWrapperClass, resourceObjectClassName, type)
            }

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            collector.build().writeTo(File(kaptKotlinGeneratedDir!!))
        }
        return true
    }

    private fun processAnnotation(element: Element, type: String) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val resourceFileSpec = ResourceObjectSpecBuilder.build(pack, className, type)
        val wrapperFileSpec = JsonApiWrapperSpecBuilder.build(pack, className, type)

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        resourceFileSpec.writeTo(File(kaptKotlinGeneratedDir!!))
        wrapperFileSpec.writeTo(File(kaptKotlinGeneratedDir))
    }
}
