package com.infinum.jsonapix.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinum.jsonapix.processor.generation.CodeOutput
import com.infinum.jsonapix.processor.ksp.extensions.holderClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Writes through KSP's code generator, with the source file of each originating model as a dependency,
 * so that KSP's incremental processing knows which models a file came from.
 *
 * Aggregate files also depend on the source files of every links, meta and error class. An incremental
 * run only processes changed files and the files that invalidated outputs depend on, so otherwise a
 * change to a model would regenerate the aggregate files without the links, meta and error classes
 * declared in unchanged files.
 */
internal class KspCodeOutput(
    private val codeGenerator: CodeGenerator,
    models: List<KSClassDeclaration>,
    linksMetaErrorClasses: List<KSClassDeclaration>,
) : CodeOutput {

    private val modelsByClassName = models.associateBy { it.holderClassName() }
    private val linksMetaErrorFiles = linksMetaErrorClasses.mapNotNull { it.containingFile }

    override fun write(fileSpec: FileSpec, originatingModels: Set<ClassName>, aggregating: Boolean) {
        val originatingFiles = originatingModels.mapTo(linkedSetOf()) { model ->
            val declaration = checkNotNull(modelsByClassName[model]) { "No declaration found for model $model." }
            checkNotNull(declaration.containingFile) { "Model $model has no source file." }
        }
        if (aggregating) {
            originatingFiles += linksMetaErrorFiles
        }
        fileSpec.writeTo(codeGenerator, aggregating, originatingFiles)
    }
}
