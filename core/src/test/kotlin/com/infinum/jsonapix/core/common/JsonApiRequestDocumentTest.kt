package com.infinum.jsonapix.core.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for the reshaping of a serialized JSON:API document into a request document.
 */
internal class JsonApiRequestDocumentTest {

    @Test
    fun `given a document with a populated included array when converting should drop included`() {
        val document = """
            {"included":[{"type":"projects","id":"252"}],"data":{"type":"translation_keys","id":"0"}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"data":{"type":"translation_keys","id":"0"}}""", result)
    }

    @Test
    fun `given a document with an errors array when converting should drop errors`() {
        val document = """
            {"errors":[{"status":400}],"data":{"type":"person","id":"1"}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"data":{"type":"person","id":"1"}}""", result)
    }

    @Test
    fun `given a document with null members at every depth when converting should drop all of them`() {
        val document = """
            {"errors":null,"links":null,"meta":null,"included":[],"data":{"type":"person","id":"0","attributes":{"name":"Jason","surname":null},"relationships":{"myFavoriteDog":{"data":{"type":"dog","id":"1"},"links":null,"meta":null},"allMyDogs":null},"links":null,"meta":null}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"0","attributes":{"name":"Jason"},"relationships":{"myFavoriteDog":{"data":{"type":"dog","id":"1"}}}}}""",
            result,
        )
    }

    @Test
    fun `given a document with a populated meta when converting should keep meta`() {
        val document = """
            {"errors":null,"links":null,"meta":{"owner":"root"},"included":[],"data":{"type":"person","id":"1"}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"meta":{"owner":"root"},"data":{"type":"person","id":"1"}}""", result)
    }

    @Test
    fun `given a document whose data is an array when converting should drop nulls from every element`() {
        val document = """
            {"errors":null,"included":[],"data":[{"type":"person","id":"1","attributes":{"name":"Jason","surname":null}},{"type":"person","id":"2","attributes":{"name":"Apix","surname":null}}]}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":[{"type":"person","id":"1","attributes":{"name":"Jason"}},{"type":"person","id":"2","attributes":{"name":"Apix"}}]}""",
            result,
        )
    }

    @Test
    fun `given a document with an empty attributes object when converting should keep the empty object`() {
        val document = """{"data":{"type":"person","id":"1","attributes":{"name":null}}}"""

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"data":{"type":"person","id":"1","attributes":{}}}""", result)
    }

    @Test
    fun `given a document that is already a request document when converting should leave it unchanged`() {
        val document = """{"data":{"type":"person","id":"1","attributes":{"name":"Jason"}}}"""

        val result = document.toJsonApiRequestDocument()

        assertEquals(document, result)
    }
}
