package com.infinum.jsonapix.processor.compile

import java.io.File

/** Settings the Gradle test task passes in as system properties. */
internal object CompileTestSettings {

    val fixturesDir: File = File(requireProperty("jsonapix.fixturesDir"))

    val goldensDir: File = File(requireProperty("jsonapix.goldensDir"))

    /** Set by `-PupdateGoldens=true`. Only then do the tests write golden files. */
    val updateGoldens: Boolean = System.getProperty("jsonapix.updateGoldens").toBoolean()

    private fun requireProperty(name: String): String =
        checkNotNull(System.getProperty(name)) { "System property '$name' is not set. Run the tests through Gradle." }
}
