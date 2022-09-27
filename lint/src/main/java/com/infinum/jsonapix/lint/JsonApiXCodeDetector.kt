package com.infinum.jsonapix.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getParentOfType

private const val JSON_API_X = "com.infinum.jsonapix.annotations.JsonApiX"
private const val JSON_API_X_META = "com.infinum.jsonapix.annotations.JsonApiXMeta"
private const val SERIALIZABLE = "kotlinx.serialization.Serializable"

@Suppress("UnstableApiUsage")
class JsonApiXCodeDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UAnnotation::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitAnnotation(node: UAnnotation) {
                if (node.qualifiedName == JSON_API_X || node.qualifiedName == JSON_API_X_META) {
                    node.getParentOfType<UAnnotated>()?.let { annotatedClass ->
                        if (annotatedClass.uAnnotations.any { it.qualifiedName == SERIALIZABLE }.not()) {
                            context.report(
                                ANNOTATION_USAGE_ISSUE,
                                node,
                                context.getLocation(node),
                                "@JsonApiX must be combined with @Serializable"
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {

        @JvmField
        val ANNOTATION_USAGE_ISSUE: Issue = Issue.create(
            id = "IllegalJsonApiXAnnotation",
            briefDescription = "Illegal use of JsonApiX annotation",
            explanation = """
                    @JsonApiX annotation takes a String type parameter and it must be combined with @Serializable 
                    from KotlinX Serialization
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                JsonApiXCodeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
