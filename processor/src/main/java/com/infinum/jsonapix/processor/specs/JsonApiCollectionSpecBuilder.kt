package com.infinum.jsonapix.processor.specs

import com.squareup.kotlinpoet.*

internal class JsonApiCollectionSpecBuilder {

    companion object {
        private const val EXTENSIONS_PACKAGE = "com.infinum.jsonapix"
        private const val EXTENSIONS_FILE_NAME = "JsonApiExtensions"
        private const val EXTENSION_FUNCTION_NAME = "toJsonApiWrapper"
    }

    private val specsMap = hashMapOf<ClassName, ClassName>()

    fun add(data: ClassName, wrapper: ClassName) {
        specsMap[data] = wrapper
    }

    fun build(): FileSpec {
        val fileSpec = FileSpec.builder(EXTENSIONS_PACKAGE, EXTENSIONS_FILE_NAME)
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class).addMember("%S", EXTENSIONS_FILE_NAME)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build()
        )
        specsMap.entries.forEach {
            fileSpec.addFunction(
                FunSpec.builder(EXTENSION_FUNCTION_NAME)
                    .receiver(it.key)
                    .returns(it.value)
                    .addStatement("return %L(this)", it.value)
                    .build()
            )
        }
        return fileSpec.build()
    }
}