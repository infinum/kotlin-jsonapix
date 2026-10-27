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
import com.infinum.jsonapix.processor.ksp.collectors.KspJsonApiXErrorCollector
import com.infinum.jsonapix.processor.ksp.collectors.KspJsonApiXLinksCollector
import com.infinum.jsonapix.processor.ksp.collectors.KspJsonApiXMetaCollector
import com.infinum.jsonapix.processor.models.JsonApiXErrorHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.JsonApiXLinksHolder
import com.infinum.jsonapix.processor.models.JsonApiXMetaHolder
import com.infinum.jsonapix.processor.validators.JsonApiXErrorValidator
import com.infinum.jsonapix.processor.validators.JsonApiXLinksValidator
import com.infinum.jsonapix.processor.validators.JsonApiXMetaValidator
import com.infinum.jsonapix.processor.validators.JsonApiXValidator
import kotlin.reflect.KClass

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
            else -> {
                val links = findAnnotatedClasses(resolver, KspJsonApiXLinksCollector.SUPPORTED)
                val meta = findAnnotatedClasses(resolver, KspJsonApiXMetaCollector.SUPPORTED)
                val errors = findAnnotatedClasses(resolver, KspJsonApiXErrorCollector.SUPPORTED)

                if (models.any { !it.validate(enableNewFeatures = false) }) {
                    // A later round only finds symbols in new files and deferred symbols, so the links,
                    // meta and error classes are deferred with the models.
                    models + links + meta + errors
                } else {
                    generate(models, links, meta, errors)
                    emptyList()
                }
            }
        }
    }

    /**
     * Finds the models the KAPT collector would collect: Kotlin classes and objects annotated with `@JsonApiX`.
     * The KAPT collector reads Kotlin metadata and only accepts what javac reports as a class.
     */
    private fun findModels(resolver: Resolver): List<KSClassDeclaration> =
        findAnnotatedClasses(resolver, setOf(JsonApiX::class))
            .filter { it.origin == Origin.KOTLIN && it.classKind in MODEL_CLASS_KINDS }

    /**
     * Finds the classes annotated with any of [annotations]. The KAPT links, meta and error collectors
     * accept every annotated class, so this doesn't filter by origin or class kind.
     */
    private fun findAnnotatedClasses(
        resolver: Resolver,
        annotations: Set<KClass<out Annotation>>,
    ): List<KSClassDeclaration> =
        annotations.asSequence()
            .flatMap { resolver.getSymbolsWithAnnotation(it.java.name) }
            .filterIsInstance<KSClassDeclaration>()
            .distinct()
            .toList()

    private fun generate(
        models: List<KSClassDeclaration>,
        links: List<KSClassDeclaration>,
        meta: List<KSClassDeclaration>,
        errors: List<KSClassDeclaration>,
    ) {
        JsonApiXGenerator(KspCodeOutput(codeGenerator, models, links + meta + errors)).generate(
            holders = collectJsonApiX(models),
            linksHolders = collectLinks(links),
            metaHolders = collectMeta(meta),
            errorHolders = collectErrors(errors),
        )
        generated = true
    }

    private fun collectJsonApiX(models: List<KSClassDeclaration>): Set<JsonApiXHolder> {
        val holders = KspJsonApiXCollector(models).collect()
        val validator = JsonApiXValidator()
        return validator.validate(holders)
    }

    private fun collectLinks(declarations: List<KSClassDeclaration>): Set<JsonApiXLinksHolder> {
        val holders = KspJsonApiXLinksCollector(declarations).collect()
        val validator = JsonApiXLinksValidator()
        return validator.validate(holders)
    }

    private fun collectMeta(declarations: List<KSClassDeclaration>): Set<JsonApiXMetaHolder> {
        val holders = KspJsonApiXMetaCollector(declarations).collect()
        val validator = JsonApiXMetaValidator()
        return validator.validate(holders)
    }

    private fun collectErrors(declarations: List<KSClassDeclaration>): Set<JsonApiXErrorHolder> {
        val holders = KspJsonApiXErrorCollector(declarations).collect()
        val validator = JsonApiXErrorValidator()
        return validator.validate(holders)
    }

    private companion object {

        // javac reports a Kotlin object as a class, so the KAPT collector treats objects as models too.
        val MODEL_CLASS_KINDS = setOf(ClassKind.CLASS, ClassKind.OBJECT)
    }
}
