package com.infinum.jsonapix.processor.behavior

import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.EmptyDataModel
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Pins today's behavior for documents without primary data: error-only and meta-only documents,
 * which have no data key, and documents whose data is null. Each is decoded into a nullable model
 * (EmptyData), a non-nullable model (Person) and a list (Person).
 *
 * Every decode throws NoSuchElementException today: with no data to read the type from, generated
 * code looks for @JsonApiX on the wrapper class, which doesn't have it. Ticket 05 changes decoding
 * on purpose and flips these tests; the comment in each one says what it becomes. Encoding already
 * succeeds today.
 */
internal class DocumentsWithoutDataIntegrationTest {

    private lateinit var factory: TypeAdapterFactory

    private val errorOnlyDocument = """
        {
            "errors": [
                {
                    "code": "not_found",
                    "title": "Not Found",
                    "detail": "The requested resource was not found",
                    "status": "404"
                }
            ]
        }
    """.trimIndent()

    private val metaOnlyDocument = """
        {
            "meta": {
                "owner": "Alice"
            }
        }
    """.trimIndent()

    private val nullDataDocument = """
        {
            "data": null,
            "links": {
                "self": "https://root.link.com"
            },
            "meta": {
                "owner": "Alice"
            }
        }
    """.trimIndent()

    @BeforeTest
    fun setup() {
        factory = TypeAdapterFactory()
    }

    // ========== ERROR-ONLY DOCUMENT ==========

    @Test
    fun `given an error-only document when decoding a nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this: data = null, with errors filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = errorOnlyDocument)
        }
    }

    @Test
    fun `given an error-only document when decoding a non-nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this to a SerializationException.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = errorOnlyDocument)
        }
    }

    @Test
    fun `given an error-only document when decoding a list should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        // Ticket 05 flips this: an empty list, with errors filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = errorOnlyDocument)
        }
    }

    // ========== META-ONLY DOCUMENT ==========

    @Test
    fun `given a meta-only document when decoding a nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this: data = null, with root meta filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = metaOnlyDocument)
        }
    }

    @Test
    fun `given a meta-only document when decoding a non-nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this to a SerializationException.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = metaOnlyDocument)
        }
    }

    @Test
    fun `given a meta-only document when decoding a list should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        // Ticket 05 flips this: an empty list, with root meta filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = metaOnlyDocument)
        }
    }

    // ========== NULL DATA DOCUMENT ==========

    @Test
    fun `given a document with null data when decoding a nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this: data = null, with root links and meta filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = nullDataDocument)
        }
    }

    @Test
    fun `given a document with null data when decoding a non-nullable model should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        // Ticket 05 flips this to a SerializationException.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = nullDataDocument)
        }
    }

    @Test
    fun `given a document with null data when decoding a list should throw NoSuchElementException`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        // Ticket 05 flips this: an empty list, with root links and meta filled.
        assertFailsWith<NoSuchElementException> {
            adapter.convertFromString(input = nullDataDocument)
        }
    }

    // ========== ENCODING ==========

    @Test
    fun `given a nullable model with null data when encoding should write a placeholder resource`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val result = adapter.convertToString(input = EmptyDataModel(data = null))

        // Unlike decoding, this doesn't throw today, but it writes data = null as a resource with
        // id "0" instead of "data": null. Ticket 05 asks only that encoding succeeds, which holds.
        assertEquals(
            """{"errors":null,"links":null,"meta":null,"included":[],"data":{"type":"emptyData","id":"0","links":null,"meta":null}}""",
            result,
        )
    }

    @Test
    fun `given an empty list when encoding should write an empty data array`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val result = adapter.convertToString(input = PersonList(data = emptyList()))

        // This already succeeds today, and ticket 05 keeps it.
        assertEquals(Fixtures.PERSON_LIST_BLANK_DATA_ENCODE, result)
    }
}
