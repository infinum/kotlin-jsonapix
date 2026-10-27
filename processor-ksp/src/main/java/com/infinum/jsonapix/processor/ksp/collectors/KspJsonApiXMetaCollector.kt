package com.infinum.jsonapix.processor.ksp.collectors

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.annotations.JsonApiXMetaList
import com.infinum.jsonapix.processor.collectors.Collector
import com.infinum.jsonapix.processor.ksp.extensions.getArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.getEnumArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.getRepeatableAnnotations
import com.infinum.jsonapix.processor.ksp.extensions.holderClassName
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder

internal class KspJsonApiXMetaCollector(
    private val declarations: List<KSClassDeclaration>,
) : Collector<JsonApiXMetaHolder> {

    override fun collect(): Set<JsonApiXMetaHolder> =
        declarations.flatMapTo(linkedSetOf()) { declaration ->
            declaration.getRepeatableAnnotations(JsonApiXMeta::class, JsonApiXMetaList::class).map { annotation ->
                JsonApiXMetaHolder(
                    type = annotation.getArgumentValue(JsonApiXMeta::type.name),
                    placementStrategy = annotation.getEnumArgumentValue(JsonApiXMeta::placementStrategy.name),
                    className = declaration.holderClassName(),
                )
            }
        }

    companion object {
        val SUPPORTED = setOf(
            JsonApiXMeta::class,
            JsonApiXMetaList::class,
        )
    }
}
