package com.infinum.onboarding.lint_rules

import com.android.tools.lint.detector.api.TextFormat
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IssueRegistryTest {

    @Test
    fun `check explanation for hamcrest import is correct`() {
        val output = IssueRegistry().issues
            .joinToString(separator = "\n") { "- **${it.id}** - ${it.getExplanation(TextFormat.RAW)}" }

        assertThat("""
        - **HamcrestImport** - Use Google Truth instead
        """.trimIndent()).isEqualTo(output)
    }
}
