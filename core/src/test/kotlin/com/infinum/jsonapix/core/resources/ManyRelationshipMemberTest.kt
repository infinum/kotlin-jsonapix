package com.infinum.jsonapix.core.resources

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests for the `data` member of a to-many relationship object.
 *
 * The JSON:API spec allows a relationship object to carry only `links` or `meta`, so `data` has to be
 * optional. An absent `data` and an empty `data` array mean different things and must stay distinguishable.
 */
internal class ManyRelationshipMemberTest {

    private val json = Json

    @Test
    fun `given a relationship object without data when deserializing should leave data null`() {
        val result = json.decodeFromString<ManyRelationshipMember>("{}")

        assertNull(result.data, "An absent data member means the relationship was not provided")
    }

    @Test
    fun `given a relationship object with an empty data array when deserializing should keep an empty list`() {
        val result = json.decodeFromString<ManyRelationshipMember>("""{"data":[]}""")

        assertEquals(emptyList<ResourceIdentifier>(), result.data, "An empty data array means no related resources")
    }

    @Test
    fun `given a relationship object with data when deserializing should keep the resource identifiers`() {
        val result = json.decodeFromString<ManyRelationshipMember>(
            """{"data":[{"type":"dog","id":"1"},{"type":"dog","id":"2"}]}""",
        )

        assertEquals(
            listOf(ResourceIdentifier(type = "dog", id = "1"), ResourceIdentifier(type = "dog", id = "2")),
            result.data,
        )
    }
}
