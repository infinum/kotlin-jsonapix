package com.infinum.jsonapix.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import java.util.EnumSet
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getParentOfType

private const val JSON_API_X_QUALIFIED_NAME = "com.infinum.jsonapix.annotations.JsonApiX"
private const val JSON_API_X_META_QUALIFIED_NAME = "com.infinum.jsonapix.annotations.JsonApiXMeta"
private const val JSON_API_X_LINKS_QUALIFIED_NAME = "com.infinum.jsonapix.annotations.JsonApiXLinks"
private const val JSON_API_X_SIMPLE_NAME = "JsonApiX"
private const val JSON_API_X_META_SIMPLE_NAME = "JsonApiXMeta"
private const val JSON_API_X_LINKS_SIMPLE_NAME = "JsonApiXLinks"
private const val SERIALIZABLE = "kotlinx.serialization.Serializable"
private const val JSON_API_X_IS_NULLABLE_ATTR = "isNullable"

@Suppress("UnstableApiUsage")
class JsonApiXCodeDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UAnnotation::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitAnnotation(node: UAnnotation) {
                when (node.qualifiedName) {
                    JSON_API_X_QUALIFIED_NAME -> validateAnnotationUsage(
                        context,
                        node,
                        JSON_API_X_SIMPLE_NAME,
                    )

                    JSON_API_X_META_QUALIFIED_NAME -> validateAnnotationUsage(
                        context,
                        node,
                        JSON_API_X_META_SIMPLE_NAME,
                    )

                    JSON_API_X_LINKS_QUALIFIED_NAME -> validateAnnotationUsage(
                        context,
                        node,
                        JSON_API_X_LINKS_SIMPLE_NAME,
                    )
                }
            }
        }
    }

    private fun validateAnnotationUsage(
        context: JavaContext,
        node: UAnnotation,
        annotationName: String,
    ) {
        if (isInvalidAnnotationUsage(node)) {
            context.report(
                createAnnotationUsageIssue(annotationName),
                node,
                context.getLocation(node),
                "$annotationName must be combined with @Serializable",
            )
        }

        if (node.getContainingUClass()?.name == "EmptyData") {
            context.log(
                exception = null,
                format = "$annotationName -> ${isPrimaryDataNullable(node)} -> ${isAllFieldsHasDefaultValues(node)}",
            )
        }

        if (annotationName == JSON_API_X_SIMPLE_NAME && isPrimaryDataNullable(node) && !isAllFieldsHasDefaultValues(
                node,
            )
        ) {
            context.report(
                createNullableDataDefaultValuesIssue(),
                node,
                context.getLocation(node),
                "Nullable $JSON_API_X_SIMPLE_NAME  attributes should have default values",
            )
        }
    }

    private fun isInvalidAnnotationUsage(node: UAnnotation): Boolean =
        node.getParentOfType<UAnnotated>()?.uAnnotations?.any { it.qualifiedName == SERIALIZABLE }
            ?.not() ?: false

    private fun isPrimaryDataNullable(annotation: UAnnotation): Boolean =
        annotation.attributeValues.firstOrNull { it.name == JSON_API_X_IS_NULLABLE_ATTR }
            ?.evaluate() as? Boolean ?: false

    private fun isAllFieldsHasDefaultValues(annotation: UAnnotation): Boolean =
        annotation.getContainingUClass()?.fields?.all { it.uastInitializer != null } ?: false

    companion object {

        @JvmField
        val annotationIssues: List<Issue> = listOf(
            createAnnotationUsageIssue(JSON_API_X_SIMPLE_NAME),
            createAnnotationUsageIssue(JSON_API_X_META_SIMPLE_NAME),
            createAnnotationUsageIssue(JSON_API_X_LINKS_SIMPLE_NAME),
            createNullableDataDefaultValuesIssue(),
        )

        @Suppress("TrimMultilineRawString")
        fun createAnnotationUsageIssue(annotationName: String): Issue =
            Issue.create(
                id = "Illegal${annotationName}Annotation",
                briefDescription = "Illegal use of $annotationName annotation",
                explanation = """
                    $annotationName must be combined with @Serializable from KotlinX Serialization as it's 
                    mandatory for correct class generation/serialization.""",
                // no need to .trimIndent(), lint does that automatically
                category = Category.CUSTOM_LINT_CHECKS,
                priority = 8,
                severity = Severity.ERROR,
                implementation = Implementation(
                    JsonApiXCodeDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                ),
            )

        @Suppress("TrimMultilineRawString")
        fun createNullableDataDefaultValuesIssue(): Issue =
            Issue.create(
                id = "IllegalNullable${JSON_API_X_SIMPLE_NAME}Annotation",
                briefDescription = "Illegal use of isNullable attr in $JSON_API_X_SIMPLE_NAME annotation",
                explanation = """
                    When setting $JSON_API_X_IS_NULLABLE_ATTR in $JSON_API_X_SIMPLE_NAME to true, all fields must have default value in case 
                    the returned data is null.
                    """, // no need to .trimIndent(), lint does that automatically
                category = Category.CUSTOM_LINT_CHECKS,
                priority = 9,
                severity = Severity.ERROR,
                implementation = Implementation(
                    JsonApiXCodeDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                ),
            )
    }
}
