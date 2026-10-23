package com.infinum.jsonapix.processor.compile

import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * Compiles every fixture with every backend and compares the generated sources with the
 * checked-in golden files. Run with `-PupdateGoldens=true` to rewrite the golden files instead.
 */
internal class GeneratedSourcesTest {

    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("fixturesAndBackends")
    fun `generated sources compile and match the golden files`(
        fixture: Fixture,
        backend: Backend,
        @TempDir workingDir: File,
    ) {
        assumeFalse(backend == Backend.KSP && fixture.name in FIXTURES_PENDING_KSP) {
            "KSP doesn't collect links, meta and error annotations yet."
        }

        val generated = compile(fixture.sources(), backend, workingDir)
        assertTrue(generated.isNotEmpty()) { "$backend generated no files for fixture '$fixture'." }

        // KAPT is the reference backend, so only its output becomes the golden files. Each fixture
        // runs on KAPT first, so the other backends are compared with the golden files it just wrote.
        if (CompileTestSettings.updateGoldens && backend == Backend.KAPT) {
            GoldenFiles(CompileTestSettings.goldensDir.resolve(fixture.name)).write(generated)
            return
        }

        assertMatchesGoldens(fixture, backend, generated)
    }

    companion object {

        /** Fixtures with links, meta or error annotations, which the KSP backend doesn't collect yet. */
        private val FIXTURES_PENDING_KSP = setOf("company", "empty-data", "links-meta-error", "person")

        @JvmStatic
        fun fixturesAndBackends(): List<Arguments> =
            Fixture.all().flatMap { fixture ->
                Backend.entries.map { backend -> Arguments.of(fixture, backend) }
            }
    }
}
