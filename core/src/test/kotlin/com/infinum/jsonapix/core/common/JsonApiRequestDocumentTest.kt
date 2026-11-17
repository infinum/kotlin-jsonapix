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

    @Test
    fun `given a relationship with a null data when converting should keep it`() {
        val document = """
            {"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null,"links":null,"meta":null}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null}}}}""",
            result,
        )
    }

    @Test
    fun `given a null relationship member when converting should still drop it`() {
        val document = """
            {"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":null,"allMyDogs":{"data":[]}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"1","relationships":{"allMyDogs":{"data":[]}}}}""",
            result,
        )
    }

    @Test
    fun `given a null data outside a relationship object when converting should drop it`() {
        val document = """
            {"data":null,"meta":{"data":null}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"meta":{}}""", result)
    }

    @Test
    fun `given a list document when converting should keep a null relationship data in every item`() {
        val document = """
            {"data":[{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null}}},{"type":"person","id":"2","relationships":{"myFavoriteDog":null}}]}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":[{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null}}},{"type":"person","id":"2","relationships":{}}]}""",
            result,
        )
    }

    @Test
    fun `given a null data member outside a relationship object when converting should drop it`() {
        val document = """
            {"data":{"type":"person","id":"1","attributes":{"data":null},"meta":{"data":null}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals("""{"data":{"type":"person","id":"1","attributes":{},"meta":{}}}""", result)
    }

    @Test
    fun `given a relationship object carrying meta with a null member when converting should drop only the meta member`() {
        val document = """
            {"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null,"meta":{"note":null}}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"data":null,"meta":{}}}}}""",
            result,
        )
    }

    @Test
    fun `given a relationship object with links and no data when converting should leave it alone`() {
        val document = """
            {"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"links":{"self":"/a"},"meta":null}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"1","relationships":{"myFavoriteDog":{"links":{"self":"/a"}}}}}""",
            result,
        )
    }

    @Test
    fun `given a to-many relationship with a null data when converting should keep it like a to-one`() {
        val document = """
            {"data":{"type":"person","id":"1","relationships":{"allMyDogs":{"data":null}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        assertEquals(
            """{"data":{"type":"person","id":"1","relationships":{"allMyDogs":{"data":null}}}}""",
            result,
        )
    }

    @Test
    fun `given a relationships key outside a resource object when converting should still treat its data as structural`() {
        val document = """
            {"meta":{"relationships":{"anything":{"data":null}}}}
        """.trimIndent()

        val result = document.toJsonApiRequestDocument()

        // The filter dispatches on the member name rather than on position in the document, so a
        // key called relationships is read as one wherever it appears. JSON:API has no such member
        // outside a resource object, and treating it as one keeps a null that would otherwise have
        // been dropped, which is the harmless direction.
        assertEquals("""{"meta":{"relationships":{"anything":{"data":null}}}}""", result)
    }
}
