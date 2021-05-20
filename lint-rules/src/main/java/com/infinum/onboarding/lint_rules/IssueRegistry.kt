package com.infinum.onboarding.lint_rules

import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import java.util.EnumSet

class IssueRegistry : com.android.tools.lint.client.api.IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(IssueAnnotationImport)

    override val api: Int = com.android.tools.lint.detector.api.CURRENT_API
}

val IssueAnnotationImport = Issue.create(
    "IssueAnnotationImport",
    "@JsonApiX annotation requires @Serializable to work",
    "Add @Serializable annotation to every class annotated with @JsonApiX",
    CORRECTNESS,
    5,
    Severity.WARNING,
    Implementation(
        AnnotationPatternDetector::class.java,
        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)
    )
)
