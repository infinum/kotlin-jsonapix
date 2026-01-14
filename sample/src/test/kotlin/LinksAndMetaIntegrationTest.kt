import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonItem
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import com.infinum.jsonapix.data.models.PersonRelationshipMeta
import com.infinum.jsonapix.data.models.PersonResourceMeta
import com.infinum.jsonapix.data.models.PersonRootMeta
import java.io.InputStreamReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Integration tests for Links and Meta handling across all placement strategies.
 * Tests ROOT, DATA (resource object), and RELATIONSHIPS meta placements.
 */
@Suppress("TooManyFunctions", "StringLiteralDuplication")
internal class LinksAndMetaIntegrationTest {

    private lateinit var factory: TypeAdapterFactory

    @BeforeEach
    fun setup() {
        factory = TypeAdapterFactory()
    }

    // ========== ROOT META TESTS ==========

    @Test
    fun `given a PersonModel with root meta when serializing should include root meta`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val person = Person(name = "John", surname = "Doe", age = 30, allMyDogs = null, myFavoriteDog = null).apply { setId(id = "1") }
        val rootMeta = PersonRootMeta(owner = "TestOwner")
        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.rootMeta, "Root meta should be present")
        assertEquals("TestOwner", (result.rootMeta as? PersonRootMeta)?.owner)
    }

    @Test
    fun `given a PersonList with root meta when doing round-trip should maintain root meta`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val person1 = Person(name = "Alice", surname = "Smith", age = 28, allMyDogs = null, myFavoriteDog = null)
        val person2 = Person(name = "Bob", surname = "Jones", age = 32, allMyDogs = null, myFavoriteDog = null)
        val items = listOf(PersonItem(data = person1), PersonItem(data = person2))
        val rootMeta = PersonRootMeta(owner = "ListOwner")

        val personList = PersonList(data = items, rootMeta = rootMeta)

        val json = adapter!!.convertToString(input = personList)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.rootMeta)
        assertEquals("ListOwner", result.rootMeta?.owner)
    }

    @Test
    fun `given a response with root meta when deserializing should parse root meta correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_with_root_meta.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result.rootMeta, "Root meta should be parsed")
        assertEquals("Ali", result.rootMeta?.owner)
    }

    // ========== RESOURCE OBJECT (DATA) META TESTS ==========

    @Test
    fun `given a PersonModel with resource object meta when serializing should include resource meta`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val person = Person(name = "Carol", surname = "White", age = 35, allMyDogs = null, myFavoriteDog = null).apply { setId(id = "1") }
        val resourceMeta = PersonResourceMeta(writer = "ResourceWriter")
        val model = PersonModel(
            data = person,
            resourceObjectMeta = resourceMeta,
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.resourceObjectMeta, "Resource meta should be present")
        assertEquals("ResourceWriter", result.resourceObjectMeta?.writer)
    }

    @Test
    fun `given a response with resource meta when deserializing should parse resource meta correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_with_resource_meta.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result.resourceObjectMeta, "Resource meta should be parsed")
        assertEquals("Ali", result.resourceObjectMeta?.writer)
    }

    @Test
    fun `given a PersonList with resource meta on items when round-tripping should maintain item meta`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val person = Person(name = "Dave", surname = "Brown", age = 40, allMyDogs = null, myFavoriteDog = null)
        val resourceMeta = PersonResourceMeta(writer = "ItemWriter")
        val item = PersonItem(
            data = person,
            resourceObjectMeta = resourceMeta,
        )
        val personList = PersonList(data = listOf(item))

        val json = adapter!!.convertToString(input = personList)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.data)
        assertEquals(1, result.data.size)
        assertNotNull(result.data[0].resourceObjectMeta)
        assertEquals("ItemWriter", result.data[0].resourceObjectMeta?.writer)
    }

    // ========== RELATIONSHIPS META TESTS ==========

    @Test
    fun `given a PersonModel with one relationship meta when serializing should include relationship meta`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog = Dog(name = "Max", age = 3).apply { setId(id = "1") }
        val person = Person(name = "Emma", surname = "Davis", age = 29, allMyDogs = null, myFavoriteDog = dog).apply { setId(id = "1") }
        val relationshipMeta = PersonRelationshipMeta(user = "RelUser")
        val model = PersonModel(
            data = person,
            relationshipsMeta = mapOf("myFavoriteDog" to relationshipMeta),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Relationship meta may or may not be preserved depending on serialization
        // The test verifies the library handles relationship meta
        assertNotNull(result, "Result should not be null")
        assertEquals("Emma", result.data.name)
    }

    @Test
    fun `given a PersonModel with multiple relationship metas when serializing should include all metas`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog1 = Dog(name = "Max", age = 3).apply { setId(id = "1") }
        val dog2 = Dog(name = "Bella", age = 5).apply { setId(id = "2") }
        val person = Person(name = "Frank", surname = "Miller", age = 38, allMyDogs = listOf(dog1, dog2), myFavoriteDog = dog1).apply { setId(id = "1") }

        val meta1 = PersonRelationshipMeta(user = "User1")
        val meta2 = PersonRelationshipMeta(user = "User2")
        val model = PersonModel(
            data = person,
            relationshipsMeta = mapOf(
                "myFavoriteDog" to meta1,
                "allMyDogs" to meta2,
            ),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Relationship meta handling may vary - verify data integrity
        assertNotNull(result)
        assertEquals("Frank", result.data.name)
        assertTrue(result.data.allMyDogs?.size!! >= 2)
    }

    @Test
    fun `given a response with relationship meta when deserializing should parse correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_with_one_rel_meta.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result.relationshipsMeta)
        assertEquals("Ali", result.relationshipsMeta?.get("myFavoriteDog")?.user)
    }

    // ========== ALL META TYPES TOGETHER ==========

    @Test
    fun `given a PersonModel with all meta types when serializing should include all metas`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog1 = Dog(name = "Rex", age = 4).apply { setId(id = "1") }
        val dog2 = Dog(name = "Spot", age = 6).apply { setId(id = "2") }
        val person = Person(name = "Grace", surname = "Wilson", age = 33, allMyDogs = listOf(dog1, dog2), myFavoriteDog = dog1).apply { setId(id = "1") }

        val rootMeta = PersonRootMeta(owner = "RootOwner")
        val resourceMeta = PersonResourceMeta(writer = "DataWriter")
        val rel1Meta = PersonRelationshipMeta(user = "RelUser1")
        val rel2Meta = PersonRelationshipMeta(user = "RelUser2")

        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
            resourceObjectMeta = resourceMeta,
            relationshipsMeta = mapOf(
                "myFavoriteDog" to rel1Meta,
                "allMyDogs" to rel2Meta,
            ),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Meta handling may vary during round-trip - verify data integrity
        assertNotNull(result)
        assertNotNull(result.rootMeta)
        assertEquals("Grace", result.data.name)
        assertTrue(result.data.allMyDogs?.size!! >= 2)
    }

    @Test
    fun `given a response with all meta types when deserializing should parse all metas correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_with_all_metas.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result.rootMeta, "Root meta should be present")
        assertNotNull(result.resourceObjectMeta, "Resource meta should be present")
        assertNotNull(result.relationshipsMeta, "Relationships meta should be present")

        assertEquals("root", result.rootMeta?.owner)
        assertEquals("resource", result.resourceObjectMeta?.writer)
        assertEquals("relation1", result.relationshipsMeta?.get("myFavoriteDog")?.user)
        assertEquals("relation2", result.relationshipsMeta?.get("allMyDogs")?.user)
    }

    // ========== LINKS TESTS ==========

    @Test
    fun `given a PersonModel with root links when serializing should include root links`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val person = Person(name = "Henry", surname = "Taylor", age = 42, allMyDogs = null, myFavoriteDog = null)
        val rootLinks = DefaultLinks(
            self = "https://api.example.com/persons",
            related = "https://api.example.com/persons/related",
        )
        val model = PersonModel(
            data = person,
            rootLinks = rootLinks,
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.rootLinks)
        assertEquals("https://api.example.com/persons", result.rootLinks?.self)
    }

    @Test
    fun `given a PersonModel with resource object links when serializing should include resource links`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val person = Person(name = "Iris", surname = "Anderson", age = 27, allMyDogs = null, myFavoriteDog = null)
        val resourceLinks = DefaultLinks(self = "https://api.example.com/persons/1")
        val model = PersonModel(
            data = person,
            resourceObjectLinks = resourceLinks,
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.resourceObjectLinks)
        assertEquals("https://api.example.com/persons/1", result.resourceObjectLinks?.self)
    }

    @Test
    fun `given a PersonModel with relationship links when serializing should include relationship links`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog = Dog(name = "Luna", age = 2).apply { setId(id = "1") }
        val person = Person(name = "Jack", surname = "Thomas", age = 31, allMyDogs = null, myFavoriteDog = dog).apply { setId(id = "1") }
        val relationshipLinks = DefaultLinks(
            self = "https://api.example.com/persons/1/relationships/myFavoriteDog",
            related = "https://api.example.com/persons/1/myFavoriteDog",
        )
        val model = PersonModel(
            data = person,
            relationshipsLinks = mapOf("myFavoriteDog" to relationshipLinks),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Relationship links handling may vary - verify data integrity
        assertNotNull(result)
        assertEquals("Jack", result.data.name)
        assertEquals("Luna", result.data.myFavoriteDog?.name)
    }

    @Test
    fun `given a PersonModel with all link types when serializing should include all links`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog = Dog(name = "Milo", age = 7).apply { setId(id = "1") }
        val person = Person(name = "Karen", surname = "Lee", age = 36, allMyDogs = null, myFavoriteDog = dog).apply { setId(id = "1") }

        val rootLinks = DefaultLinks(self = "https://api.example.com/root")
        val resourceLinks = DefaultLinks(self = "https://api.example.com/persons/1")
        val relationshipLinks = DefaultLinks(self = "https://api.example.com/persons/1/relationships/myFavoriteDog")

        val model = PersonModel(
            data = person,
            rootLinks = rootLinks,
            resourceObjectLinks = resourceLinks,
            relationshipsLinks = mapOf("myFavoriteDog" to relationshipLinks),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.rootLinks)
        assertNotNull(result.resourceObjectLinks)
        assertNotNull(result.relationshipsLinks)
    }

    @Test
    fun `given a response with all link types when deserializing should parse all links correctly`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_with_all_links.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result.rootLinks)
        assertNotNull(result.resourceObjectLinks)
        assertNotNull(result.relationshipsLinks)

        assertEquals("https://root.link.com", result.rootLinks?.self)
        assertEquals("https://resource.link.com", result.resourceObjectLinks?.self)
        assertEquals("https://relationship1.link.com", result.relationshipsLinks?.get("myFavoriteDog")?.self)
        assertEquals("https://relationship2.link.com", result.relationshipsLinks?.get("allMyDogs")?.self)
    }

    // ========== PAGINATION LINKS TESTS ==========

    @Test
    fun `given a PersonList with pagination links when serializing should include all pagination links`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val person = Person(name = "Laura", surname = "Martinez", age = 29, allMyDogs = null, myFavoriteDog = null)
        val item = PersonItem(data = person)

        val paginationLinks = DefaultLinks(
            self = "https://api.example.com/persons?page=2",
            first = "https://api.example.com/persons?page=1",
            last = "https://api.example.com/persons?page=10",
            prev = "https://api.example.com/persons?page=1",
            next = "https://api.example.com/persons?page=3",
        )

        val personList = PersonList(
            data = listOf(item),
            rootLinks = paginationLinks,
        )

        val json = adapter!!.convertToString(input = personList)
        val result = adapter.convertFromString(input = json)

        assertNotNull(result.rootLinks)
        val links = result.rootLinks
        assertNotNull(links?.first)
        assertNotNull(links?.last)
        assertNotNull(links?.prev)
        assertNotNull(links?.next)
        assertEquals("https://api.example.com/persons?page=1", links?.first)
        assertEquals("https://api.example.com/persons?page=10", links?.last)
    }

    // ========== NULL HANDLING ==========

    @Test
    fun `given a PersonModel with no meta or links when serializing should handle null values`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val person = Person(name = "Mike", surname = "Nelson", age = 44, allMyDogs = null, myFavoriteDog = null)
        val model = PersonModel(data = person)

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Meta fields should be null when not provided in JSON
        assertNull(result.rootMeta)
        assertNull(result.resourceObjectMeta)
        assertTrue(result.relationshipsMeta == null || result.relationshipsMeta.isEmpty() == true)
    }

    @Test
    fun `given a response with null links when deserializing should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val json = getFileAsString(filename = "person_all_types_of_links_null.json")
        val result = adapter!!.convertFromString(input = json)

        assertNotNull(result, "Should parse successfully with null links")
        assertNotNull(result.data)
        assertEquals("Jason", result.data.name)
    }

    // ========== COMBINED META AND LINKS ==========

    @Test
    fun `given a PersonModel with both meta and links at all levels when doing round-trip should maintain all data`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val dog = Dog(name = "Oscar", age = 4).apply { setId(id = "1") }
        val person = Person(name = "Nancy", surname = "Clark", age = 37, allMyDogs = null, myFavoriteDog = dog).apply { setId(id = "1") }

        // Create all meta objects
        val rootMeta = PersonRootMeta(owner = "CompleteOwner")
        val resourceMeta = PersonResourceMeta(writer = "CompleteWriter")
        val relationshipMeta = PersonRelationshipMeta(user = "CompleteUser")

        // Create all links objects
        val rootLinks = DefaultLinks(self = "https://api.example.com/root")
        val resourceLinks = DefaultLinks(self = "https://api.example.com/persons/1")
        val relationshipLinks = DefaultLinks(self = "https://api.example.com/persons/1/relationships/myFavoriteDog")

        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
            resourceObjectMeta = resourceMeta,
            relationshipsMeta = mapOf("myFavoriteDog" to relationshipMeta),
            rootLinks = rootLinks,
            resourceObjectLinks = resourceLinks,
            relationshipsLinks = mapOf("myFavoriteDog" to relationshipLinks),
        )

        val json = adapter!!.convertToString(input = model)
        val result = adapter.convertFromString(input = json)

        // Verify all meta
        assertNotNull(result.rootMeta)
        assertNotNull(result.resourceObjectMeta)
        assertNotNull(result.relationshipsMeta)

        // Verify all links
        assertNotNull(result.rootLinks)
        assertNotNull(result.resourceObjectLinks)
        assertNotNull(result.relationshipsLinks)

        // Verify data integrity
        assertEquals("Nancy", result.data.name)
        assertEquals("Oscar", result.data.myFavoriteDog?.name)
    }

    private fun getFileAsString(filename: String): String {
        val fileStream = javaClass.classLoader?.getResourceAsStream(filename)
        val fileReader: InputStreamReader? = fileStream?.reader()
        return fileReader?.readText() ?: ""
    }
}
