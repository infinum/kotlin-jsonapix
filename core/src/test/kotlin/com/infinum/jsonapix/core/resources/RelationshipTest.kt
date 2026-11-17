package com.infinum.jsonapix.core.resources

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@Serializable
private data class TestDog(val name: String)

@Serializable
private data class TestOwner(
    val name: String,
    val favorite: Relationship<TestDog> = Relationship.Unset,
)

internal class RelationshipTest {

    @Test
    fun `given a To relationship when reading value should return the model`() {
        val dog = TestDog(name = "Max")

        assertEquals(dog, Relationship.Included(dog).value)
    }

    @Test
    fun `given any state other than To when reading value should return null`() {
        assertNull(Relationship.Unset.value)
        assertNull(Relationship.Clear.value)
        assertNull(Relationship.Linked(ResourceIdentifier(type = "dog", id = "1")).value)
    }

    @Test
    fun `given two relationships pointing at the same resource should be equal`() {
        val identifier = ResourceIdentifier(type = "dog", id = "1")

        assertEquals(Relationship.Linked(identifier), Relationship.Linked(identifier))
        assertEquals(Relationship.Included(TestDog(name = "Max")), Relationship.Included(TestDog(name = "Max")))
    }

    // A model declaring a Relationship property has to keep compiling under @Serializable, which
    // means kotlinx has to be able to resolve a serializer for it. JsonApiX never serializes a
    // model through this path, but the compiler plugin still demands one exists.
    @Test
    fun `given a serializable model holding a Relationship should resolve a serializer`() {
        val json = Json { encodeDefaults = true }

        val encoded = json.encodeToString(TestOwner(name = "Ana", favorite = Relationship.Included(TestDog(name = "Max"))))

        assertNotNull(encoded)
        assertEquals(TestOwner(name = "Ana", favorite = Relationship.Included(TestDog(name = "Max"))), json.decodeFromString<TestOwner>(encoded))
    }
}
