package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.annotations.JsonApiXErrorList
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValues
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

internal class JsonApiXErrorCollector(
    private val roundEnvironment: RoundEnvironment,
    private val elementUtils: Elements,
) : Collector<JsonApiXErrorHolder> {

    override fun collect(): Set<JsonApiXErrorHolder> {
        val holders = mutableSetOf<JsonApiXErrorHolder>()

        // Collect from repeated annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXErrorList::class.java)?.forEach { element ->
            val types = element.getAnnotationParameterValues<JsonApiXError, List<String>> {
                this.map { annotation -> annotation.type }
            }

            types.forEach { type ->
                holders.add(
                    JsonApiXErrorHolder(
                        type = type,
                        className = ClassName(
                            elementUtils.getPackageOf(element).toString(),
                            element.simpleName.toString(),
                        ),
                    ),
                )
            }
        }

        // Collect from single annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXError::class.java)?.forEach { element ->
            val type = element.getAnnotationParameterValue<JsonApiXError, String> { type }

            holders.add(
                JsonApiXErrorHolder(
                    type = type,
                    className = ClassName(
                        elementUtils.getPackageOf(element).toString(),
                        element.simpleName.toString(),
                    ),
                ),
            )
        }

        return holders
    }

    companion object {
        val SUPPORTED = setOf(
            JsonApiXError::class.java.name,
            JsonApiXErrorList::class.java.name,
        )
    }
}
