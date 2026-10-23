package com.infinum.jsonapix.processor.ksp.subprocessors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.validate
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.generation.JsonApiXGenerator
import com.infinum.jsonapix.processor.ksp.KspCodeOutput
import com.infinum.jsonapix.processor.ksp.collectors.KspJsonApiXCollector
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.validators.JsonApiXValidator

/**
 * Generates the code for all models at once, because the aggregate files depend on every model.
 * A model can reference a type that another processor generates in a later round, so while any model
 * doesn't resolve, all models are deferred. Generation then happens exactly once, in the first round
 * where every model resolves.
 */
internal class KspJsonApiXSubprocessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    private var generated = false

    // javac reports a Kotlin object as a class, so the KAPT collector treats objects as models too.
    val MODEL_CLASS_KINDS = setOf(ClassKind.CLASS, ClassKind.OBJECT)

    /** Processes one round and returns the symbols to defer to the next one. */
    fun process(resolver: Resolver): List<KSAnnotated> {
        val models = findModels(resolver)
        return when {
            models.isEmpty() -> emptyList()
            generated -> {
                models.forEach { model ->
                    logger.error("JsonApiX code was already generated in an earlier round, so it can't include this model.", model)
                }
                emptyList()
            }
            models.any { !it.validate(enableNewFeatures = false) } -> models
            else -> {
                generate(models)
                emptyList()
            }
        }
    }

    /** Finds the models the KAPT collector would collect: Kotlin classes and objects annotated with `@JsonApiX`. */
    private fun findModels(resolver: Resolver): List<KSClassDeclaration> =
        resolver.getSymbolsWithAnnotation(JsonApiX::class.java.name)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.origin == Origin.KOTLIN && it.classKind in MODEL_CLASS_KINDS }
            .toList()

    private fun generate(models: List<KSClassDeclaration>) {
        JsonApiXGenerator(KspCodeOutput(codeGenerator, models)).generate(
            holders = collectJsonApiX(models),
            // Links, meta and error annotations aren't collected under KSP yet.
            linksHolders = emptySet(),
            metaHolders = emptySet(),
            errorHolders = emptySet(),
        )
        generated = true
    }

    private fun collectJsonApiX(models: List<KSClassDeclaration>): Set<JsonApiXHolder> {
        val holders = KspJsonApiXCollector(models).collect()
        val validator = JsonApiXValidator()
        return validator.validate(holders)
    }

    private companion object {

    }
}
