package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.annotations.JsonApiXMetaList
import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValues
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

internal class JsonApiXMetaCollector(
    private val roundEnvironment: RoundEnvironment,
    private val elementUtils: Elements,
) : Collector<JsonApiXMetaHolder> {

    override fun collect(): Set<JsonApiXMetaHolder> {
        val holders = mutableSetOf<JsonApiXMetaHolder>()

        // Collect from repeated annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXMetaList::class.java)?.forEach { element ->
            val typeAndPlacementList =
                element.getAnnotationParameterValues<JsonApiXMeta, List<Pair<String, MetaPlacementStrategy>>> {
                    this.map { annotation -> annotation.type to annotation.placementStrategy }
                }

            typeAndPlacementList.forEach { (type, placementStrategy) ->
                holders.add(
                    JsonApiXMetaHolder(
                        type = type,
                        placementStrategy = placementStrategy,
                        className = ClassName(
                            elementUtils.getPackageOf(element).toString(),
                            element.simpleName.toString(),
                        ),
                    ),
                )
            }
        }

        // Collect from single annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXMeta::class.java)?.forEach { element ->
            val (type, placementStrategy) =
                element.getAnnotationParameterValue<JsonApiXMeta, Pair<String, MetaPlacementStrategy>> {
                    type to placementStrategy
                }

            holders.add(
                JsonApiXMetaHolder(
                    type = type,
                    placementStrategy = placementStrategy,
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
            JsonApiXMeta::class.java.name,
            JsonApiXMetaList::class.java.name,
        )
    }
}
