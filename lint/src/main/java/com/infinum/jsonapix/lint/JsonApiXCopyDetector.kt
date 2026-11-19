package com.infinum.jsonapix.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import java.util.EnumSet
import org.jetbrains.uast.UCallExpression

private const val JSON_API_X_QUALIFIED_NAME = "com.infinum.jsonapix.annotations.JsonApiX"
private const val COPY = "copy"
private const val COPY_MODEL = "copyModel"

/**
 * Reports `copy` on a model that extends `JsonApiModel`, and points at the generated `copyModel`.
 *
 * A model's JSON:API `type` and `id` live in private fields on `JsonApiModel` and are set after
 * construction, so they are not constructor parameters. `copy` builds a new instance through the
 * constructor and leaves both behind, and the copy then serializes through the `id() ?: "0"`
 * fallback. The request goes out claiming to be resource `0` rather than failing, which is why
 * this is worth a check: nothing else catches it.
 */
@Suppress("UnstableApiUsage")
class JsonApiXCopyDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(COPY)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val containingClass = method.containingClass ?: return

        // Keyed off the annotation rather than the JsonApiModel supertype. Walking the supertypes
        // from here sends lint through PsiClass.getExtendsListTypes, which fails to resolve Kotlin
        // source classes under this AGP and brings the whole analysis down. The annotation is also
        // the more precise condition: it leaves the generated wrappers alone, and OwnerModel holds
        // no identity of its own, so copying one is safe.
        if (containingClass.annotations.none { it.qualifiedName == JSON_API_X_QUALIFIED_NAME }) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "`$COPY` drops the JSON:API `type` and `id`, so the copy serializes as " +
                "resource `0`. Use the generated `$COPY_MODEL` instead.",
            quickfixData = quickFix(),
        )
    }

    private fun quickFix(): LintFix = LintFix.create()
        .name("Replace with $COPY_MODEL")
        .replace()
        .text(COPY)
        .with(COPY_MODEL)
        .build()

    companion object {

        @Suppress("TrimMultilineRawString")
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "JsonApiXCopyLosesIdentity",
                briefDescription = "copy() on a JsonApiX model drops its type and id",
                explanation = """
                    A JsonApiX model keeps its JSON:API `type` and `id` in private fields on `JsonApiModel`,
                    set after construction rather than passed to the constructor. `copy` rebuilds the model
                    through its constructor, so the copy carries neither, and serializing it falls back to
                    an `id` of `0`. A request built that way is sent against the wrong resource instead of
                    failing.

                    The processor generates `copyModel` for every annotated model. It takes the same named
                    arguments as `copy`, defaults each one to the current value, and carries the `type` and
                    `id` across. Models that are not data classes have no `copy` at all and have always
                    needed it.

                    Suppress this only where the copy is meant to become a different resource, and set the
                    identity yourself with `setId`.""",
                // no need to .trimIndent(), lint does that automatically
                category = Category.CORRECTNESS,
                priority = 7,
                severity = Severity.WARNING,
                implementation = Implementation(
                    JsonApiXCopyDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                ),
            )

        @JvmField
        val issues: List<Issue> = listOf(ISSUE)
    }
}
