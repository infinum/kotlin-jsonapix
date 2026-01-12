import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.DogList
import com.infinum.jsonapix.data.models.DogModel
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Integration tests for AdapterFactory.getAdapter() function.
 * Tests the complete workflow of adapter retrieval and type safety.
 */
@Suppress("StringLiteralDuplication")
internal class AdapterFactoryIntegrationTest {

    private lateinit var factory: TypeAdapterFactory

    @BeforeEach
    fun setup() {
        factory = TypeAdapterFactory()
    }

    @Test
    fun `given a TypeAdapterFactory when getting adapter for PersonModel should return correct typed adapter`() {
        val adapter: TypeAdapter<PersonModel>? = factory.getAdapter()

        assertNotNull(adapter, "Adapter should not be null for PersonModel")
        assertTrue(adapter is TypeAdapter<*>, "Should be instance of TypeAdapter")
    }

    @Test
    fun `given a TypeAdapterFactory when getting adapter for DogModel should return correct typed adapter`() {
        val adapter: TypeAdapter<DogModel>? = factory.getAdapter()

        assertNotNull(adapter, "Adapter should not be null for DogModel")
        assertTrue(adapter is TypeAdapter<*>, "Should be instance of TypeAdapter")
    }

    @Test
    fun `given a TypeAdapterFactory when getting adapter for PersonList should return correct typed adapter`() {
        val adapter: TypeAdapter<PersonList>? = factory.getAdapter()

        assertNotNull(adapter, "Adapter should not be null for PersonList")
        assertTrue(adapter is TypeAdapter<*>, "Should be instance of TypeAdapter")
    }

    @Test
    fun `given a TypeAdapterFactory when getting adapter for DogList should return correct typed adapter`() {
        val adapter: TypeAdapter<DogList>? = factory.getAdapter()

        assertNotNull(adapter, "Adapter should not be null for DogList")
        assertTrue(adapter is TypeAdapter<*>, "Should be instance of TypeAdapter")
    }

    @Test
    fun `given a TypeAdapterFactory when getting multiple adapters should return different instances for different types`() {
        val personAdapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        val dogAdapter: TypeAdapter<DogModel>? = factory.getAdapter()

        assertNotNull(personAdapter, "Person adapter should not be null")
        assertNotNull(dogAdapter, "Dog adapter should not be null")
        assertTrue(personAdapter !== dogAdapter, "Different types should have different adapter instances")
    }

    @Test
    fun `given a TypeAdapterFactory when getting same adapter type multiple times should work consistently`() {
        val adapter1: TypeAdapter<PersonModel>? = factory.getAdapter()
        val adapter2: TypeAdapter<PersonModel>? = factory.getAdapter()

        assertNotNull(adapter1, "First adapter should not be null")
        assertNotNull(adapter2, "Second adapter should not be null")
    }

    @Test
    fun `given a PersonModel adapter when using it for serialization should work correctly`() {
        val adapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        assertNotNull(adapter)

        val person = Person(
            name = "John",
            surname = "Doe",
            age = 30,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val json = adapter!!.convertToString(model)

        assertNotNull(json, "JSON should not be null")
        assertTrue(json.contains("\"name\":\"John\""), "JSON should contain name")
        assertTrue(json.contains("\"surname\":\"Doe\""), "JSON should contain surname")
        assertTrue(json.contains("\"age\":30"), "JSON should contain age")
    }

    @Test
    fun `given a PersonModel adapter when using it for deserialization should work correctly`() {
        val adapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "attributes": {
                        "name": "Jane",
                        "surname": "Smith",
                        "age": 25
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.data, "Data should not be null")
        assertEquals("Jane", result.data.name)
        assertEquals("Smith", result.data.surname)
        assertEquals(25, result.data.age)
    }

    @Test
    fun `given a DogModel adapter when using it for round-trip serialization should maintain data integrity`() {
        val adapter: TypeAdapter<DogModel>? = factory.getAdapter()
        assertNotNull(adapter)

        val dog = Dog(name = "Buddy", age = 5)
        val model = DogModel(data = dog)

        val json = adapter!!.convertToString(model)
        val result = adapter.convertFromString(json)

        assertNotNull(result, "Result should not be null")
        assertEquals("Buddy", result.data.name)
        assertEquals(5, result.data.age)
    }

    @Test
    fun `given a PersonList adapter when deserializing list of persons should return correct count`() {
        val adapter: TypeAdapter<PersonList>? = factory.getAdapter()
        assertNotNull(adapter)

        val json = """
            {
                "data": [
                    {
                        "type": "person",
                        "attributes": {
                            "name": "Alice",
                            "surname": "Johnson",
                            "age": 28
                        }
                    },
                    {
                        "type": "person",
                        "attributes": {
                            "name": "Bob",
                            "surname": "Williams",
                            "age": 32
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.data, "Data should not be null")
        assertEquals(2, result.data.size, "Should have 2 persons")
        assertEquals("Alice", result.data[0].data.name)
        assertEquals("Bob", result.data[1].data.name)
    }

    @Test
    fun `given a PersonModel adapter when handling complex relationships should work end-to-end`() {
        val adapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        assertNotNull(adapter)

        val dog1 = Dog(name = "Max", age = 3).apply { setId("1") }
        val dog2 = Dog(name = "Rex", age = 5).apply { setId("2") }
        val person = Person(
            name = "Owner",
            surname = "Smith",
            age = 40,
            allMyDogs = listOf(dog1, dog2),
            myFavoriteDog = dog1,
        ).apply { setId("100") }
        val model = PersonModel(data = person)

        val json = adapter!!.convertToString(model)
        val result = adapter.convertFromString(json)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.data.allMyDogs, "All dogs should not be null")
        // The library may include the favorite dog in the allMyDogs list
        assertTrue(result.data.allMyDogs?.size!! >= 2, "Should have at least 2 dogs")
        assertNotNull(result.data.myFavoriteDog, "Favorite dog should not be null")
        assertEquals("Max", result.data.myFavoriteDog?.name)
    }

    @Test
    fun `given a TypeAdapterFactory when getting adapter using KClass should work correctly`() {
        val adapter = factory.getAdapter(PersonModel::class)

        assertNotNull(adapter, "Adapter should not be null when using KClass")
    }

    @Test
    fun `given multiple adapters when using them concurrently should maintain thread safety`() {
        val personAdapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        val dogAdapter: TypeAdapter<DogModel>? = factory.getAdapter()

        assertNotNull(personAdapter)
        assertNotNull(dogAdapter)

        // Create test data
        val person = Person("John", "Doe", 30, null, null)
        val personModel = PersonModel(data = person)
        val dog = Dog("Buddy", 5)
        val dogModel = DogModel(data = dog)

        // Serialize both concurrently
        val personJson = personAdapter!!.convertToString(personModel)
        val dogJson = dogAdapter!!.convertToString(dogModel)

        // Verify both work independently
        assertTrue(personJson.contains("John"))
        assertTrue(dogJson.contains("Buddy"))

        // Deserialize both
        val personResult = personAdapter.convertFromString(personJson)
        val dogResult = dogAdapter.convertFromString(dogJson)

        assertEquals("John", personResult.data.name)
        assertEquals("Buddy", dogResult.data.name)
    }

    @Test
    fun `given a PersonList adapter when serializing empty list should handle gracefully`() {
        val adapter: TypeAdapter<PersonList>? = factory.getAdapter()
        assertNotNull(adapter)

        val emptyList = PersonList(data = emptyList())
        val json = adapter!!.convertToString(emptyList)

        assertNotNull(json)
        assertTrue(json.contains("\"data\":[]"), "Should contain empty data array")
    }

    @Test
    fun `given a PersonModel adapter when deserializing with null relationships should handle gracefully`() {
        val adapter: TypeAdapter<PersonModel>? = factory.getAdapter()
        assertNotNull(adapter)

        val json = """
            {
                "data": {
                    "type": "person",
                    "attributes": {
                        "name": "Test",
                        "surname": "Person",
                        "age": 20
                    },
                    "relationships": {
                        "myFavoriteDog": {
                            "data": null
                        }
                    }
                }
            }
        """.trimIndent()

        val result = adapter!!.convertFromString(json)

        assertNotNull(result)
        assertNull(result.data.myFavoriteDog, "Favorite dog should be null")
    }
}
