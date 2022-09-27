package com.infinum.jsonapix.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage", "MaxLineLength")
class JsonApiXCodeDetectorTest : LintDetectorTest() {

    object Stubs {
        val TEST: TestFile = kotlin(
            """
                    package com.infinum.jsonapix.lint

                    import com.infinum.jsonapix.annotations.JsonApiX

                    @JsonApiX(type = "company")
                    class TestClass1 (val someValue: String)
                    """
        ).indented()
    }

    @Test
    fun testMissingAnnotation() {
        lint().files(Stubs.TEST)
            .issues(JsonApiXCodeDetector.ANNOTATION_USAGE_ISSUE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.java:5 Error: @JsonApiX must be combined with @Serializable [IllegalJsonApiXAnnotation]
                        @JsonApiX(type = "company")
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~

                    Lint found errors in the project; aborting build.
                    """
            )
    }

    override fun getDetector(): Detector = JsonApiXCodeDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(JsonApiXCodeDetector.ANNOTATION_USAGE_ISSUE)
}
