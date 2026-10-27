package com.infinum.jsonapix.processor.ksp.collectors

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.annotations.JsonApiXErrorList
import com.infinum.jsonapix.processor.collectors.Collector
import com.infinum.jsonapix.processor.ksp.extensions.getArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.getRepeatableAnnotations
import com.infinum.jsonapix.processor.ksp.extensions.holderClassName
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder

internal class KspJsonApiXErrorCollector(
    private val declarations: List<KSClassDeclaration>,
) : Collector<JsonApiXErrorHolder> {

    override fun collect(): Set<JsonApiXErrorHolder> =
        declarations.flatMapTo(linkedSetOf()) { declaration ->
            declaration.getRepeatableAnnotations(JsonApiXError::class, JsonApiXErrorList::class).map { annotation ->
                JsonApiXErrorHolder(
                    type = annotation.getArgumentValue(JsonApiXError::type.name),
                    className = declaration.holderClassName(),
                )
            }
        }

    companion object {
        val SUPPORTED = setOf(
            JsonApiXError::class,
            JsonApiXErrorList::class,
        )
    }
}
