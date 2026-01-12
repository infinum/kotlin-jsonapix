import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonModel
import com.infinum.jsonapix.data.models.PersonRelationshipMeta
import com.infinum.jsonapix.data.models.PersonResourceMeta
import com.infinum.jsonapix.data.models.PersonRootMeta
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.io.InputStreamReader

internal class TypeAdapterTest {

    private var typeAdapter: TypeAdapter<PersonModel>? = null

    @BeforeEach
    fun setup() {
        typeAdapter = TypeAdapterFactory().getAdapter()
    }

    @org.junit.jupiter.api.Test
    fun `given that response has both myFavoriteDog one and allMyDogs many rel set type adapter Person convertFromString should generate a Person class with allMyDogs and myFavouriteDog set`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        )

        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "myFavoriteDog" to DefaultLinks(self = "https://relationship.link.com"),
                "allMyDogs" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = getFileAsString("person_one_and_many_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response had no included block but both relationships set type adapter Person convertFromString should generate a Person class with allMyDogs and myFavoriteDog as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
        )

        val response = getFileAsString("person_no_included_block.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response has no allMyDogs many rel set type adapter Person convertFromString should generate a Person class with one myFavoriteDog relationship and allMyDogs as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1)
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "myFavoriteDog" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = getFileAsString("person_one_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response has allMyDogs many rel set as null type adapter Person convertFromString should generate a Person class with one myFavoriteDog relationship and allMyDogs as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1)
        )
        val personModel = PersonModel(
            person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "myFavoriteDog" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = getFileAsString("person_many_rel_null_with_included.json")
        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response has allMyDogs many rel set but no myFavoriteDog one rel set type adapter Person convertFromString should generate a Person class with valid allMyDogs list and myFavouriteDog as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = null
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "allMyDogs" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = getFileAsString("person_many_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but all level links set as null in response type adapter Person convertFromString should generate a valid person class with no illegal argument exception`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = null
        )
        val personModel = PersonModel(person)

        val response = getFileAsString("person_all_types_of_links_null.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personModel,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is a null relationship data in response type adapter Person convertFromString should handle it gracefully`() {
        val response = getFileAsString("person_invalid_relationship_data.json")

        val result = typeAdapter?.convertFromString(response)

        // Verify that null relationship data is handled gracefully
        Assertions.assertNotNull(result)
        Assertions.assertEquals("Jason", result?.data?.name)
        Assertions.assertEquals("Apix", result?.data?.surname)
        Assertions.assertEquals(28, result?.data?.age)
        Assertions.assertNull(result?.data?.myFavoriteDog) // Should be null per JSON:API spec
        Assertions.assertEquals(2, result?.data?.allMyDogs?.size) // Should have 2 dogs
    }

    @org.junit.jupiter.api.Test
    fun `given that person has both allMyDogs and myFavoriteDog set type adapter Person convertToString should generate a json with many allMyDogs relationships and myFavouriteDog relationship but links as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("0") }, Dog(name = "Bongo", age = 2).apply { setId("0") }),
            myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") }
        ).apply {
            setId("0")
        }

        val model = PersonModel(
            data = person,
        )
        val response = getFileAsString("person_no_links_all_rel.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person has myFavoriteDog set as null but allMyDogs as an empty list type adapter Person convertToString should generate a json with one relationship null and many as empty`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            myFavoriteDog = null,
            allMyDogs = emptyList(),
        )

        val model = PersonModel(
            data = person,
        )

        val response = getFileAsString("person_one_rel_null_many_rel_empty.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that Person has both allMyDogs and myFavoriteDog set as null type adapter Person convertToString should generate a json with both one and many rel as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        )

        val model = PersonModel(
            data = person,
        )

        val response = getFileAsString("person_one_and_many_rel_as_null.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given a Person with allMyDogs with id set type adapter Person convertToString should generate a json with allMyDogs many rel and correct id set for each dog in both included and relationship blocks`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = null
        )

        val model = PersonModel(
            data = person,
        )

        val response = getFileAsString("person_all_my_dogs_with_id_set_for_each_dog.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }


    @org.junit.jupiter.api.Test
    fun `given that a response that has a root meta, should generate a Person with PersonRootMeta`() {
        val rootMeta = PersonRootMeta("Ali")
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null,
        )

        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
        )

        val response = getFileAsString("person_with_root_meta.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model.rootMeta,
            result?.rootMeta
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that a response that has a resource object meta, should generate a Person with PersonResourceMeta`() {
        val resourceMeta = PersonResourceMeta("Ali")
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null,
        )

        val model = PersonModel(
            data = person,
            resourceObjectMeta = resourceMeta,
        )

        val response = getFileAsString("person_with_resource_meta.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model.resourceObjectMeta,
            result?.resourceObjectMeta
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that a response that has a one relationship meta, should generate a Person with PersonRelationshipsMeta`() {
        val relationshipMeta = PersonRelationshipMeta("Ali")
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1),
        )

        val model = PersonModel(
            data = person,
            relationshipsMeta = mapOf("myFavoriteDog" to relationshipMeta)
        )

        val response = getFileAsString("person_with_one_rel_meta.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model.relationshipsMeta,
            result?.relationshipsMeta
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that a response that has multiple relationship meta, should generate a Person with multiple PersonRelationshipsMeta`() {
        val firstMeta = PersonRelationshipMeta("First")
        val secondMeta = PersonRelationshipMeta("Second")
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = Dog(name = "Bella", age = 1),
        )

        val model = PersonModel(
            data = person,
            relationshipsMeta = mapOf(
                "myFavoriteDog" to firstMeta,
                "allMyDogs" to secondMeta,
            )
        )

        val response = getFileAsString("person_with_many_rel_meta.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model.relationshipsMeta,
            result?.relationshipsMeta
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that a response that has multiple  all types of meta, should generate a Person with all types of meta`() {
        val rootMeta = PersonRootMeta("root")
        val resourceMeta = PersonResourceMeta("resource")
        val relationship1Meta = PersonRelationshipMeta("relation1")
        val relationship2Meta = PersonRelationshipMeta("relation2")

        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = Dog(name = "Bella", age = 1),
        ).apply { setId("1") }

        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
            resourceObjectMeta = resourceMeta,
            relationshipsMeta = mapOf(
                "myFavoriteDog" to relationship1Meta,
                "allMyDogs" to relationship2Meta,
            )
        )

        val response = getFileAsString("person_with_all_metas.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that a response that has multiple  all types of links, should generate a Person with all types of links`() {
        val rootLinks = DefaultLinks("https://root.link.com")
        val resourceLinks = DefaultLinks("https://resource.link.com")
        val relationship1Links = DefaultLinks("https://relationship1.link.com")
        val relationship2Links = DefaultLinks("https://relationship2.link.com")

        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = Dog(name = "Bella", age = 1),
        )

        val model = PersonModel(
            data = person,
            rootLinks = rootLinks,
            resourceObjectLinks = resourceLinks,
            relationshipsLinks = mapOf(
                "myFavoriteDog" to relationship1Links,
                "allMyDogs" to relationship2Links,
            )
        )

        val response = getFileAsString("person_with_all_links.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            model,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that Person with all types of meta, should generate a a response that has all types of meta`() {
        val rootMeta = PersonRootMeta("root")
        val resourceMeta = PersonResourceMeta("resource")
        val relationship1Meta = PersonRelationshipMeta("relation1")
        val relationship2Meta = PersonRelationshipMeta("relation2")

        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = Dog(name = "Bella", age = 1),
        ).apply { setId("1") }

        val model = PersonModel(
            data = person,
            rootMeta = rootMeta,
            resourceObjectMeta = resourceMeta,
            relationshipsMeta = mapOf(
                "myFavoriteDog" to relationship1Meta,
                "allMyDogs" to relationship2Meta,
            )
        )

        val response = getFileAsString("person_with_all_meta_types_encode.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that Person with all types of links, should generate a a response that has all types of meta`() {
        val rootLinks = DefaultLinks("root")
        val resourceLinks = DefaultLinks("resource")
        val relationship1Links = DefaultLinks("relation1")
        val relationship2Links = DefaultLinks("relation2")

        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = Dog(name = "Bella", age = 1),
        ).apply { setId("1") }

        val model = PersonModel(
            data = person,
            rootLinks = rootLinks,
            resourceObjectLinks = resourceLinks,
            relationshipsLinks = mapOf(
                "myFavoriteDog" to relationship1Links,
                "allMyDogs" to relationship2Links,
            )
        )

        val response = getFileAsString("person_with_all_links_types_encode.json")

        val result = typeAdapter?.convertToString(model)

        Assertions.assertEquals(
            response,
            result
        )
    }
    private fun getFileAsString(filename: String): String {
        val fileStream = javaClass.classLoader?.getResourceAsStream(filename)
        val fileReader: InputStreamReader? = fileStream?.reader()
        return fileReader?.readText() ?: ""
    }
}