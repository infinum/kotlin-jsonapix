package com.infinum.jsonapix.processor.compile

import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/**
 * Proves that a module without Retrofit compiles and gets no Retrofit code. The processor itself always
 * has Retrofit, so this only holds if the backends look Retrofit up on the consumer's compile classpath.
 * The golden files cover the other case, because the other compile tests have Retrofit on their classpath.
 */
internal class NoRetrofitCompileTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(Backend::class)
    fun `a module without Retrofit compiles and gets no Retrofit code`(backend: Backend, @TempDir workingDir: File) {
        val fixture = Fixture.named("links-meta-error")

        val generated = compile(fixture.sources(), backend, workingDir, excludedPackages = RETROFIT_PACKAGES)

        assertTrue(generated.isNotEmpty()) { "$backend generated no files for fixture '$fixture'." }
        assertNoJvmOnlyApis(fixture, backend, generated, retrofitExtensionAllowed = false)
    }

    private companion object {

        /** JsonApiX's `retrofit` artifact and Retrofit itself. */
        val RETROFIT_PACKAGES = setOf("com.infinum.jsonapix.retrofit", "retrofit2")
    }
}
