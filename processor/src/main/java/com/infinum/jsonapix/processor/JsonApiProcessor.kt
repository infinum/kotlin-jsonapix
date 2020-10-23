package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.JsonApiSerializable
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
        roundEnv?.getElementsAnnotatedWith(JsonApiSerializable::class.java)
            ?.forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes can be annotated"
                    )
                    return true
                }
                processAnnotation(it)
            }
        return false
    }

    private fun processAnnotation(element: Element) {
        val type = element.getAnnotationParameterValue<JsonApiSerializable, String> { type }
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val dataClass = ClassName(pack, className)
        val serialName = ClassName("kotlinx.serialization", "SerialName")
        val serializable = ClassName("kotlinx.serialization", "Serializable")
        val file = FileSpec.builder(pack, "JsonApiSerializable_$className")
            .addType(
                TypeSpec.classBuilder("JsonApiSerializable_$className")
                    .addSuperinterface(
                        JsonApiWrapper::class.asClassName().parameterizedBy(dataClass)
                    )
                    .addAnnotation(serializable)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("data", dataClass).build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "id", Int::class, KModifier.OVERRIDE
                        ).addAnnotation(
                            AnnotationSpec.builder(serialName)
                                .addMember("value = %S", "id").build()
                        )
                            .initializer("%L", 0).build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "type", String::class, KModifier.OVERRIDE
                        ).addAnnotation(
                            AnnotationSpec.builder(serialName)
                                .addMember("value = %S", "type").build()
                        )
                            .initializer("%S", type).build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "data", dataClass
                        ).addAnnotation(
                            AnnotationSpec.builder(serialName)
                                .addMember("value = %S", "data").build()
                        )
                            .initializer("data").addModifiers(KModifier.OVERRIDE)
                            .build()
                    )
                    .build()
            )
            .build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        file.writeTo(File(kaptKotlinGeneratedDir!!))
    }
}

interface JsonApiWrapper<out T> {
    val id: Int
    val type: String
    val data: T
}