package com.infinum.jsonapix.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.infinum.jsonapix.processor.ksp.subprocessors.KspJsonApiXSubprocessor

internal class JsonApiXSymbolProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
) : SymbolProcessor {

    private val jsonApiXSubprocessor = KspJsonApiXSubprocessor(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> = jsonApiXSubprocessor.process(resolver)
}
