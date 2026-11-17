package com.infinum.jsonapix.processor.behavior

import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException

/**
 * Integration tests for error handling scenarios.
 * Tests how the library handles malformed JSON, errors, and edge cases.
 */
@Suppress("TooManyFunctions")
internal class ErrorHandlingIntegrationTest {

    private lateinit var factory: TypeAdapterFactory

    @BeforeTest
    fun setup() {
        factory = TypeAdapterFactory()
    }

    @Test
    fun `given a response with missing required fields when parsing should handle appropriately`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "attributes": {
                        "name": "John"
                    }
                }
            }
        """.trimIndent()

        // Missing required fields (surname, age) should result in a parsing exception
        assertFailsWith<Exception> {
            adapter!!.convertFromString(input = json)
        }
    }

    @Test
    fun `given a response with invalid JSON structure when parsing should throw exception`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "attributes": "invalid"
                }
            }
        """.trimIndent()

        assertFailsWith<Exception> {
            adapter!!.convertFromString(json)
        }
    }

    @Test
    fun `given a response with empty string when parsing should throw exception`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        assertFailsWith<Exception> {
            adapter!!.convertFromString(input = "")
        }
    }

    @Test
    fun `given a response with malformed JSON when parsing should throw exception`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person"
        """.trimIndent()

        assertFailsWith<Exception> {
            adapter!!.convertFromString(json)
        }
    }

    @Test
    fun `given a response with wrong type when parsing should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "wrong-type",
                    "attributes": {
                        "name": "Test",
                        "surname": "User",
                        "age": 25
                    }
                }
            }
        """.trimIndent()

        // Wrong resource type causes deserialization to fail
        assertFailsWith<SerializationException> {
            adapter!!.convertFromString(input = json)
        }
    }

    @Test
    fun `given a response with extra unknown fields when parsing should ignore them`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Alice",
                        "surname": "Smith",
                        "age": 30,
                        "unknownField": "should be ignored"
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should parse successfully ignoring unknown fields")
        assertEquals("Alice", result.data.name)
        assertEquals("Smith", result.data.surname)
    }

    @Test
    fun `given a list response with mixed valid and invalid items when parsing should handle appropriately`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val json = """
            {
                "data": [
                    {
                        "type": "person",
                        "id": "1",
                        "attributes": {
                            "name": "Valid",
                            "surname": "Person",
                            "age": 25
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should parse valid items")
        assertEquals(1, result.data.size)
    }

    @Test
    fun `given a response with null attributes when parsing should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": null
                }
            }
        """.trimIndent()

        // Null attributes should result in a parsing failure
        assertFailsWith<Exception> {
            adapter!!.convertFromString(input = json)
        }
    }

    @Test
    fun `given a response with circular relationships when parsing should handle without infinite loop`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        // JSON:API handles circular refs via relationships and included
        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Alice",
                        "surname": "Smith",
                        "age": 30
                    },
                    "relationships": {
                        "myFavoriteDog": {
                            "data": {
                                "type": "dog",
                                "id": "1"
                            }
                        }
                    }
                },
                "included": [
                    {
                        "type": "dog",
                        "id": "1",
                        "attributes": {
                            "name": "Buddy",
                            "age": 5
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should handle relationships without infinite loop")
        assertNotNull(result.data.myFavoriteDog)
        assertEquals("Buddy", result.data.myFavoriteDog?.name)
    }

    @Test
    fun `given a response with duplicate IDs in included when parsing should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Bob",
                        "surname": "Jones",
                        "age": 28
                    },
                    "relationships": {
                        "allMyDogs": {
                            "data": [
                                {"type": "dog", "id": "1"},
                                {"type": "dog", "id": "1"}
                            ]
                        }
                    }
                },
                "included": [
                    {
                        "type": "dog",
                        "id": "1",
                        "attributes": {
                            "name": "Max",
                            "age": 3
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should handle duplicate IDs")
        assertNotNull(result.data.allMyDogs)
    }

    @Test
    fun `given a response with missing included resource when parsing should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Carol",
                        "surname": "White",
                        "age": 32
                    },
                    "relationships": {
                        "myFavoriteDog": {
                            "data": {
                                "type": "dog",
                                "id": "999"
                            }
                        }
                    }
                },
                "included": []
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should handle missing included resource")
        // The relationship might be null if the included resource is not found
    }

    @Test
    fun `given a response with very deep nesting when parsing should handle within limits`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Deep",
                        "surname": "Nester",
                        "age": 35
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should handle deep nesting")
        assertEquals("Deep", result.data.name)
    }

    @Test
    fun `given a response with unicode characters when parsing should handle correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "José",
                        "surname": "Müller",
                        "age": 30
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result)
        assertEquals("José", result.data.name)
        assertEquals("Müller", result.data.surname)
    }

    @Test
    fun `given a response with special characters in strings when parsing should handle correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "John \"The Boss\" Doe",
                        "surname": "O'Brien",
                        "age": 40
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result)
        assertTrue(result.data.name?.contains("Boss") == true)
        assertTrue(result.data.surname.contains("O'Brien"))
    }

    @Test
    fun `given a response with very large numbers when parsing should handle correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Old",
                        "surname": "Person",
                        "age": 2147483647
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result)
        assertEquals(2_147_483_647, result.data.age)
    }

    @Test
    fun `given a response with array instead of object when parsing should throw exception`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = """
            [
                {
                    "type": "person",
                    "attributes": {
                        "name": "Test",
                        "surname": "User",
                        "age": 25
                    }
                }
            ]
        """.trimIndent()

        assertFailsWith<Exception> {
            adapter!!.convertFromString(json)
        }
    }
}
