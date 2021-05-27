package com.infinum.onboarding.lint_rules

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

class AnnotationDetectorTest {

    @Test
    fun `should not report imports that are not required`() {
        TestLintTask.lint()
            .files(TestFiles.java("""
            package foo;
            import foo.R;
            class Example {
            }""").indented())
            .issues(IssueAnnotationImport)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun `should warn about required import`() {
        TestLintTask.lint()
            .files(TestFiles.java("""
          package foo;
          import com.infinum.jsonapix.annotations.JsonApiX;
          class Example {
          }""").indented())
            .issues(IssueAnnotationImport)
            .allowMissingSdk()
            .run()
            .expect("""
          |src/foo/Example.java:2: Warning: Required import [IssueAnnotationImport]
          |import com.infinum.jsonapix.annotations.JsonApiX;
          |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin())
    }
}
