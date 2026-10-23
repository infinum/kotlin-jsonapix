package com.infinum.jsonapix.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinum.jsonapix.processor.generation.CodeOutput
import com.infinum.jsonapix.processor.ksp.extensions.modelClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Writes through KSP's code generator, with the source file of each originating model as a dependency,
 * so that KSP's incremental processing knows which models a file came from.
 */
internal class KspCodeOutput(
    private val codeGenerator: CodeGenerator,
    models: List<KSClassDeclaration>,
) : CodeOutput {

    private val modelsByClassName = models.associateBy { it.modelClassName() }

    override fun write(fileSpec: FileSpec, originatingModels: Set<ClassName>, aggregating: Boolean) {
        val originatingFiles = originatingModels.mapTo(linkedSetOf()) { model ->
            val declaration = checkNotNull(modelsByClassName[model]) { "No declaration found for model $model." }
            checkNotNull(declaration.containingFile) { "Model $model has no source file." }
        }
        fileSpec.writeTo(codeGenerator, aggregating, originatingFiles)
    }
}
