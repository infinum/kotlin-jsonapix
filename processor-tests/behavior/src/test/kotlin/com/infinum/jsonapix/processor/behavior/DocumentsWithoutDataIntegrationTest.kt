package com.infinum.jsonapix.processor.behavior

import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.EmptyDataMeta
import com.infinum.jsonapix.data.models.EmptyDataModel
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import com.infinum.jsonapix.data.models.PersonRootMeta
import com.infinum.jsonapix.data.models.PersonalError
import com.infinum.jsonapix.decodeJsonApiXString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException

/**
 * Documents without primary data: error-only and meta-only documents, which have no data key, and
 * documents whose data is null. Each is decoded into a nullable model (EmptyData), a non-nullable
 * model (Person) and a list (Person).
 *
 * A nullable model decodes to data = null and a list to an empty list, both with the document's
 * root meta, links and errors. A non-nullable model can't be built without data, so decoding
 * throws a SerializationException.
 */
@Suppress("TooManyFunctions")
internal class DocumentsWithoutDataIntegrationTest {

    private lateinit var factory: TypeAdapterFactory

    // Each error has the fields of both error classes: DefaultError for EmptyData and
    // PersonalError for Person.
    private val errorOnlyDocument = """
        {
            "errors": [
                {
                    "code": "not_found",
                    "title": "Not Found",
                    "detail": "The requested resource was not found",
                    "status": "404",
                    "desc": "Not found"
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
    fun `given an error-only document when decoding a nullable model should return null data with errors`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = errorOnlyDocument)

        assertNull(result.data)
        val error = result.errors?.single()
        assertNotNull(error)
        assertEquals("not_found", error.code)
        assertEquals("Not Found", error.title)
        assertEquals("The requested resource was not found", error.detail)
        assertEquals("404", error.status)
    }

    @Test
    fun `given an error-only document when decoding a non-nullable model should throw SerializationException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        assertFailsWith<SerializationException> {
            adapter.convertFromString(input = errorOnlyDocument)
        }
    }

    @Test
    fun `given an error-only document when decoding a list should return an empty list with errors`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = errorOnlyDocument)

        assertTrue(result.data.isEmpty())
        assertEquals(listOf(PersonalError(desc = "Not found")), result.errors)
    }

    // ========== META-ONLY DOCUMENT ==========

    @Test
    fun `given a meta-only document when decoding a nullable model should return null data with root meta`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = metaOnlyDocument)

        assertNull(result.data)
        assertEquals(EmptyDataMeta(owner = "Alice"), result.rootMeta)
    }

    @Test
    fun `given a meta-only document when decoding a non-nullable model should throw SerializationException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        assertFailsWith<SerializationException> {
            adapter.convertFromString(input = metaOnlyDocument)
        }
    }

    @Test
    fun `given a meta-only document when decoding a list should return an empty list with root meta`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = metaOnlyDocument)

        assertTrue(result.data.isEmpty())
        assertEquals(PersonRootMeta(owner = "Alice"), result.rootMeta)
    }

    // ========== NULL DATA DOCUMENT ==========

    @Test
    fun `given a document with null data when decoding a nullable model should return null data with root links and meta`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = nullDataDocument)

        assertNull(result.data)
        assertEquals("https://root.link.com", result.rootLinks?.self)
        assertEquals(EmptyDataMeta(owner = "Alice"), result.rootMeta)
    }

    @Test
    fun `given a document with null data when decoding a non-nullable model should throw SerializationException`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        assertFailsWith<SerializationException> {
            adapter.convertFromString(input = nullDataDocument)
        }
    }

    @Test
    fun `given a document with null data when decoding a list should return an empty list with root links and meta`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val result = adapter.convertFromString(input = nullDataDocument)

        assertTrue(result.data.isEmpty())
        assertEquals("https://root.link.com", result.rootLinks?.self)
        assertEquals(PersonRootMeta(owner = "Alice"), result.rootMeta)
    }

    // ========== ENCODING ==========

    @Test
    fun `given a nullable model with null data when encoding should write null data`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val result = adapter.convertToString(input = EmptyDataModel(data = null))

        assertEquals(
            """{"errors":null,"links":null,"meta":null,"data":null,"included":[]}""",
            result,
        )
    }

    @Test
    fun `given a nullable model with null data when doing round-trip should keep null data and root meta`() {
        val adapter = factory.getAdapter<EmptyDataModel>()
        assertNotNull(adapter)

        val model = EmptyDataModel(data = null, rootMeta = EmptyDataMeta(owner = "Alice"))
        val result = adapter.convertFromString(input = adapter.convertToString(input = model))

        assertNull(result.data)
        assertEquals(EmptyDataMeta(owner = "Alice"), result.rootMeta)
    }

    @Test
    fun `given an empty list when encoding should write an empty data array`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val result = adapter.convertToString(input = PersonList(data = emptyList()))

        assertEquals(Fixtures.PERSON_LIST_BLANK_DATA_ENCODE, result)
    }

    // ========== UNKNOWN CLASS ==========

    @Test
    fun `given a class without a JSON API type when decoding a document without data should throw naming the class`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            errorOnlyDocument.decodeJsonApiXString<NotAModel, NotAModel>(
                rootLinks = "",
                resourceObjectLinks = "",
                relationshipsLinks = "",
                rootMeta = "",
                resourceObjectMeta = "",
                relationshipsMeta = "",
                errors = "",
            )
        }

        assertTrue(exception.message.orEmpty().startsWith("NotAModel "), exception.message)
    }

    private class NotAModel
}
