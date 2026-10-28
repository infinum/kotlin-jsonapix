package com.infinum.jsonapix.processor.compile

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import java.io.File

private const val DIFF_CONTEXT_LINES = 3

/** The golden files of one fixture, keyed by their path relative to [dir]. */
internal class GoldenFiles(private val dir: File) {

    fun read(): Map<String, String> = readSourceTree(dir)

    /** Replaces all golden files of the fixture with [files], removing ones that are no longer generated. */
    fun write(files: Map<String, String>) {
        dir.deleteRecursively()
        files.forEach { (path, text) ->
            val file = dir.resolve(path)
            file.parentFile.mkdirs()
            file.writeText(text)
        }
    }
}

/** Reads every file under [root], keyed by its path relative to [root]. */
internal fun readSourceTree(root: File): Map<String, String> =
    root.walkTopDown()
        .filter { it.isFile && !it.isHidden }
        .associate { it.relativeTo(root).invariantSeparatorsPath to it.readText() }

/**
 * Describes each file that differs between [golden] and [generated]: a unified diff for changed
 * files, and one line for files that only exist on one side. Returns an empty list if all files match.
 */
internal fun describeDifferences(
    golden: Map<String, String>,
    generated: Map<String, String>,
    goldenLabel: String,
    generatedLabel: String,
): List<String> =
    (golden.keys + generated.keys).sorted().mapNotNull { path ->
        val expected = golden[path]
        val actual = generated[path]
        when {
            expected == null -> "$generatedLabel/$path was generated but has no golden file."
            actual == null -> "$goldenLabel/$path has a golden file but was not generated."
            expected != actual -> unifiedDiff(expected, actual, "$goldenLabel/$path", "$generatedLabel/$path")
            else -> null
        }
    }

private fun unifiedDiff(expected: String, actual: String, expectedLabel: String, actualLabel: String): String {
    val expectedLines = expected.split("\n")
    val actualLines = actual.split("\n")
    val patch = DiffUtils.diff(expectedLines, actualLines)
    return UnifiedDiffUtils.generateUnifiedDiff(expectedLabel, actualLabel, expectedLines, patch, DIFF_CONTEXT_LINES)
        .joinToString("\n")
}
