package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiSerializable
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.specs.JsonApiExtensionsSpecBuilder
import com.infinum.jsonapix.processor.specs.JsonApiWrapperSpecBuilder
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
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(JsonApiSerializable::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val collector = JsonApiExtensionsSpecBuilder()
        val elements = roundEnv?.getElementsAnnotatedWith(JsonApiSerializable::class.java)
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
                processAnnotation(it)
                val className = it.simpleName.toString()
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                val dataClass = ClassName(pack, className)
                val generatedName = "JsonApiSerializable_$className"
                val wrapperClass = ClassName(pack, generatedName)
                collector.add(dataClass, wrapperClass)
            }

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            collector.build().writeTo(File(kaptKotlinGeneratedDir!!))
        }
        return true
    }

    private fun processAnnotation(element: Element) {
        val type = element.getAnnotationParameterValue<JsonApiSerializable, String> { type }
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileSpec = JsonApiWrapperSpecBuilder.build(pack, className, type)

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        fileSpec.writeTo(File(kaptKotlinGeneratedDir!!))
    }
}

