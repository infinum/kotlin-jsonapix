package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.annotations.JsonApiXLinks
import com.infinum.jsonapix.annotations.JsonApiXLinksList
import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValues
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

internal class JsonApiXLinksCollector(
    private val roundEnvironment: RoundEnvironment,
    private val elementUtils: Elements
) : Collector<JsonApiXLinksHolder> {

    companion object {
        val SUPPORTED = setOf(
            JsonApiXLinks::class.java.name,
            JsonApiXLinksList::class.java.name
        )
    }

    override fun collect(): Set<JsonApiXLinksHolder> {
        val holders = mutableSetOf<JsonApiXLinksHolder>()

        // Collect from repeated annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXLinksList::class.java)?.forEach { element ->
            val typeAndPlacementList =
                element.getAnnotationParameterValues<JsonApiXLinks, List<Pair<String, LinksPlacementStrategy>>> {
                    this.map { annotation -> annotation.type to annotation.placementStrategy }
                }

            typeAndPlacementList.forEach { (type, placementStrategy) ->
                holders.add(
                    JsonApiXLinksHolder(
                        type = type,
                        placementStrategy = placementStrategy,
                        className = ClassName(
                            elementUtils.getPackageOf(element).toString(),
                            element.simpleName.toString()
                        )
                    )
                )
            }
        }

        // Collect from single annotations
        roundEnvironment.getElementsAnnotatedWith(JsonApiXLinks::class.java)?.forEach { element ->
            val (type, placementStrategy) =
                element.getAnnotationParameterValue<JsonApiXLinks, Pair<String, LinksPlacementStrategy>> {
                    type to placementStrategy
                }

            holders.add(
                JsonApiXLinksHolder(
                    type = type,
                    placementStrategy = placementStrategy,
                    className = ClassName(
                        elementUtils.getPackageOf(element).toString(),
                        element.simpleName.toString()
                    )
                )
            )
        }

        return holders
    }
}
