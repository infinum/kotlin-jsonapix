package com.infinum.jsonapix.processor.compile

import com.tschuchort.compiletesting.SourceFile
import java.io.File

/**
 * A directory of model sources that are compiled together. Each fixture has its own set of golden
 * files, so a fixture includes every model its models reference.
 */
internal class Fixture(val name: String, private val dir: File) {

    /** The fixture's source files, sorted by name. */
    fun files(): List<File> =
        dir.listFiles { file -> file.extension == "kt" }
            .orEmpty()
            .sortedBy { it.name }

    fun sources(): List<SourceFile> = files().map { it.toSourceFile() }

    override fun toString(): String = name

    companion object {

        fun all(): List<Fixture> =
            CompileTestSettings.fixturesDir.listFiles(File::isDirectory)
                .orEmpty()
                .sortedBy { it.name }
                .map { Fixture(it.name, it) }

        fun named(name: String): Fixture = all().single { it.name == name }
    }
}

internal fun File.toSourceFile(): SourceFile = SourceFile.kotlin(name, readText(), trimIndent = false)
