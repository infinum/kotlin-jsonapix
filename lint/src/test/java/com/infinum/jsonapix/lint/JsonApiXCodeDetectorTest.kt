package com.infinum.jsonapix.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class JsonApiXCodeDetectorTest: LintDetectorTest() {

    object Stubs {
        val TEST: TestFile = kotlin(
            """
                    package com.infinum.jsonapix.lint

                    import com.infinum.jsonapix.annotations.JsonApiX

                    @JsonApiX
                    class TestClass1 (val someValue: String)
                    """
        ).indented()
    }

    @Test
    fun testBasic() {
        lint().files(Stubs.TEST)
            .issues(JsonApiXCodeDetector.ANNOTATION_USAGE_ISSUE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.java:5: ERROR: Illegal use of JsonApiX annotation
                        private static String s2 = "Let's say it: lint";
                                                   ~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            )
    }

    override fun getDetector(): Detector = JsonApiXCodeDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(JsonApiXCodeDetector.ANNOTATION_USAGE_ISSUE)
}
