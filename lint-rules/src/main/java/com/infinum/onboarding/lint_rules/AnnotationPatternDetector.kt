package com.infinum.onboarding.lint_rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UImportStatement

class AnnotationPatternDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes() = listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext) = AnnotationInvalidImportHandler(context)
}

class AnnotationInvalidImportHandler(private val context: JavaContext) : UElementHandler() {
    override fun visitImportStatement(node: UImportStatement) {
        node.importReference?.let { importReference ->
            if (importReference.asSourceString()
                    .contains("com.infinum.jsonapix.annotations.JsonApiX")
            ) {
                if (!importReference.asSourceString()
                        .contains("kotlinx.serialization.Serializable")
                ) {
                    context.report(
                        IssueAnnotationImport,
                        node,
                        context.getLocation(importReference),
                        "Required import"
                    )
                }
            }
        }
    }
}
