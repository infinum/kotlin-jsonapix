package com.infinum.jsonapix.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Integration tests for JsonApiModel public functions.
 * Tests the complete workflow of type and id management in JSON:API resource objects.
 */
internal class JsonApiModelIntegrationTest {

    // Test model implementation
    private class TestModel : JsonApiModel()

    @Test
    fun `given a new model instance when getting type should return null`() {
        val model = TestModel()

        val result = model.type()

        assertNull(result, "Type should be null for a newly created model")
    }

    @Test
    fun `given a new model instance when getting id should return null`() {
        val model = TestModel()

        val result = model.id()

        assertNull(result, "ID should be null for a newly created model")
    }

    @Test
    fun `given a model when setting type should store and retrieve correctly`() {
        val model = TestModel()
        val expectedType = "test-resource"

        model.setType(type = expectedType)
        val result = model.type()

        assertEquals(expectedType, result, "Type should match the set value")
    }

    @Test
    fun `given a model when setting id should store and retrieve correctly`() {
        val model = TestModel()
        val expectedId = "123"

        model.setId(id = expectedId)
        val result = model.id()

        assertEquals(expectedId, result, "ID should match the set value")
    }

    @Test
    fun `given a model when setting both type and id should store both correctly`() {
        val model = TestModel()
        val expectedType = "article"
        val expectedId = "456"

        model.setType(type = expectedType)
        model.setId(id = expectedId)

        assertEquals(expectedType, model.type(), "Type should match the set value")
        assertEquals(expectedId, model.id(), "ID should match the set value")
    }

    @Test
    fun `given a model with type set when updating type should replace old value`() {
        val model = TestModel()
        val initialType = "person"
        val updatedType = "user"

        model.setType(type = initialType)
        assertEquals(initialType, model.type())

        model.setType(type = updatedType)
        val result = model.type()

        assertEquals(updatedType, result, "Type should be updated to new value")
        assertNotEquals(initialType, result, "Old type should be replaced")
    }

    @Test
    fun `given a model with id set when updating id should replace old value`() {
        val model = TestModel()
        val initialId = "100"
        val updatedId = "200"

        model.setId(id = initialId)
        assertEquals(initialId, model.id())

        model.setId(id = updatedId)
        val result = model.id()

        assertEquals(updatedId, result, "ID should be updated to new value")
        assertNotEquals(initialId, result, "Old ID should be replaced")
    }

    @Test
    fun `given a model when setting type to null should clear type`() {
        val model = TestModel()
        model.setType(type = "some-type")
        assertNotNull(model.type())

        model.setType(type = null)
        val result = model.type()

        assertNull(result, "Type should be null after clearing")
    }

    @Test
    fun `given a model when setting id to null should clear id`() {
        val model = TestModel()
        model.setId(id = "some-id")
        assertNotNull(model.id())

        model.setId(id = null)
        val result = model.id()

        assertNull(result, "ID should be null after clearing")
    }

    @Test
    fun `given multiple model instances when setting type and id should maintain separate state`() {
        val model1 = TestModel()
        val model2 = TestModel()

        model1.setType(type = "person")
        model1.setId(id = "1")
        model2.setType(type = "article")
        model2.setId(id = "2")

        assertEquals("person", model1.type(), "Model 1 should maintain its own type")
        assertEquals("1", model1.id(), "Model 1 should maintain its own ID")
        assertEquals("article", model2.type(), "Model 2 should maintain its own type")
        assertEquals("2", model2.id(), "Model 2 should maintain its own ID")
    }

    @Test
    fun `given a model when setting type with special characters should handle correctly`() {
        val model = TestModel()
        val specialType = "complex-type_with-special.chars"

        model.setType(type = specialType)
        val result = model.type()

        assertEquals(specialType, result, "Should handle special characters in type")
    }

    @Test
    fun `given a model when setting id with special characters should handle correctly`() {
        val model = TestModel()
        val specialId = "uuid-123e4567-e89b-12d3-a456-426614174000"

        model.setId(id = specialId)
        val result = model.id()

        assertEquals(specialId, result, "Should handle special characters in ID")
    }

    @Test
    fun `given a model when setting empty string type should store empty string`() {
        val model = TestModel()

        model.setType(type = "")
        val result = model.type()

        assertEquals("", result, "Should store empty string as type")
    }

    @Test
    fun `given a model when setting empty string id should store empty string`() {
        val model = TestModel()

        model.setId(id = "")
        val result = model.id()

        assertEquals("", result, "Should store empty string as ID")
    }

    @Test
    fun `given a model when setting very long type string should handle correctly`() {
        val model = TestModel()
        val longType = "a".repeat(1000)

        model.setType(type = longType)
        val result = model.type()

        assertEquals(longType, result, "Should handle very long type strings")
        assertEquals(1000, result?.length, "Should maintain full length")
    }

    @Test
    fun `given a model when setting very long id string should handle correctly`() {
        val model = TestModel()
        val longId = "1".repeat(1000)

        model.setId(id = longId)
        val result = model.id()

        assertEquals(longId, result, "Should handle very long ID strings")
        assertEquals(1000, result?.length, "Should maintain full length")
    }

    @Test
    fun `given a model when setting type multiple times should always use latest value`() {
        val model = TestModel()

        model.setType(type = "type1")
        model.setType(type = "type2")
        model.setType(type = "type3")
        val result = model.type()

        assertEquals("type3", result, "Should use the latest set value")
    }

    @Test
    fun `given a model when setting id multiple times should always use latest value`() {
        val model = TestModel()

        model.setId(id = "id1")
        model.setId(id = "id2")
        model.setId(id = "id3")
        val result = model.id()

        assertEquals("id3", result, "Should use the latest set value")
    }

    @Test
    fun `given a model with type and id when clearing one should not affect the other`() {
        val model = TestModel()
        model.setType(type = "article")
        model.setId(id = "123")

        model.setType(type = null)

        assertNull(model.type(), "Type should be cleared")
        assertEquals("123", model.id(), "ID should remain unchanged")
    }

    @Test
    fun `given a model with type and id when clearing id should not affect type`() {
        val model = TestModel()
        model.setType(type = "article")
        model.setId(id = "123")

        model.setId(id = null)

        assertEquals("article", model.type(), "Type should remain unchanged")
        assertNull(model.id(), "ID should be cleared")
    }
}
