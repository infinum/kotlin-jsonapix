package com.infinum.jsonapix.processor.specs

import com.squareup.kotlinpoet.*

internal class JsonApiCollectionSpecBuilder {

    private val specsMap = hashMapOf<ClassName, ClassName>()

    fun add(data: ClassName, wrapper: ClassName) {
        specsMap[data] = wrapper
    }

    fun build(): FileSpec {
        val fileSpec = FileSpec.builder("com.infinum.jsonapix", "JsonApiExtensions")

        specsMap.entries.forEach {
            fileSpec.addFunction(
                FunSpec.builder("getWrapper")
                    .receiver(it.key)
                    .returns(it.value)
                    .addStatement("return %L(this)", it.value)
                    .build()
            )
        }
        return fileSpec.build()
    }
}