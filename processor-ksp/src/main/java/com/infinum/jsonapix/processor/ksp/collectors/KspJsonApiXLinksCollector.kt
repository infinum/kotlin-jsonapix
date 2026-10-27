package com.infinum.jsonapix.processor.ksp.collectors

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinum.jsonapix.annotations.JsonApiXLinks
import com.infinum.jsonapix.annotations.JsonApiXLinksList
import com.infinum.jsonapix.processor.collectors.Collector
import com.infinum.jsonapix.processor.ksp.extensions.getArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.getEnumArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.getRepeatableAnnotations
import com.infinum.jsonapix.processor.ksp.extensions.holderClassName
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder

internal class KspJsonApiXLinksCollector(
    private val declarations: List<KSClassDeclaration>,
) : Collector<JsonApiXLinksHolder> {

    override fun collect(): Set<JsonApiXLinksHolder> =
        declarations.flatMapTo(linkedSetOf()) { declaration ->
            declaration.getRepeatableAnnotations(JsonApiXLinks::class, JsonApiXLinksList::class).map { annotation ->
                JsonApiXLinksHolder(
                    type = annotation.getArgumentValue(JsonApiXLinks::type.name),
                    placementStrategy = annotation.getEnumArgumentValue(JsonApiXLinks::placementStrategy.name),
                    className = declaration.holderClassName(),
                )
            }
        }

    companion object {
        val SUPPORTED = setOf(
            JsonApiXLinks::class,
            JsonApiXLinksList::class,
        )
    }
}
