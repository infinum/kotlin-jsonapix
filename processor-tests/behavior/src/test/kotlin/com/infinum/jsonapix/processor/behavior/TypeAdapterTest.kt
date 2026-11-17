package com.infinum.jsonapix.processor.behavior

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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TypeAdapterTest {

    private var typeAdapter: TypeAdapter<PersonModel>? = null

    @BeforeTest
    fun setup() {
        typeAdapter = TypeAdapterFactory().getAdapter()
    }

    @Test
    fun `given that response has both myFavoriteDog one and allMyDogs many rel set type adapter Person convertFromString should generate a Person class with allMyDogs and myFavouriteDog set`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1),
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

        val response = Fixtures.PERSON_ONE_AND_MANY_REL

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that response had no included block but both relationships set type adapter Person convertFromString should generate a Person class with allMyDogs and myFavoriteDog as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
        )

        val response = Fixtures.PERSON_NO_INCLUDED_BLOCK

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that response has no allMyDogs many rel set type adapter Person convertFromString should generate a Person class with one myFavoriteDog relationship and allMyDogs as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1),
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "myFavoriteDog" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = Fixtures.PERSON_ONE_REL

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that response has allMyDogs many rel set as null type adapter Person convertFromString should generate a Person class with one myFavoriteDog relationship and allMyDogs as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1),
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "myFavoriteDog" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = Fixtures.PERSON_MANY_REL_NULL_WITH_INCLUDED
        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that response has allMyDogs many rel set but no myFavoriteDog one rel set type adapter Person convertFromString should generate a Person class with valid allMyDogs list and myFavouriteDog as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = null,
        )
        val personModel = PersonModel(
            data = person,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
            resourceObjectLinks = DefaultLinks(self = "https://resource.link.com"),
            relationshipsLinks = mapOf(
                "allMyDogs" to DefaultLinks(self = "https://relationship.link.com"),
            ),
        )

        val response = Fixtures.PERSON_MANY_REL

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that there is an included block but all level links set as null in response type adapter Person convertFromString should generate a valid person class with no illegal argument exception`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = null,
        )
        val personModel = PersonModel(data = person)

        val response = Fixtures.PERSON_ALL_TYPES_OF_LINKS_NULL

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            personModel,
            result,
        )
    }

    @Test
    fun `given that there is a null relationship data in response type adapter Person convertFromString should handle it gracefully`() {
        val response = Fixtures.PERSON_INVALID_RELATIONSHIP_DATA

        val result = typeAdapter?.convertFromString(input = response)

        // Verify that null relationship data is handled gracefully
        assertNotNull(result)
        assertEquals("Jason", result?.data?.name)
        assertEquals("Apix", result?.data?.surname)
        assertEquals(28, result?.data?.age)
        assertNull(result?.data?.myFavoriteDog) // Should be null per JSON:API spec
        assertEquals(2, result?.data?.allMyDogs?.size) // Should have 2 dogs
    }

    @Test
    fun `given that person has both allMyDogs and myFavoriteDog set type adapter Person convertToString should generate a json with many allMyDogs relationships and myFavouriteDog relationship but links as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("0") }, Dog(name = "Bongo", age = 2).apply { setId("0") }),
            myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") },
        ).apply {
            setId("0")
        }

        val model = PersonModel(
            data = person,
        )
        val response = Fixtures.PERSON_NO_LINKS_ALL_REL

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }

    @Test
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

        val response = Fixtures.PERSON_ONE_REL_NULL_MANY_REL_EMPTY

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }

    @Test
    fun `given that Person has both allMyDogs and myFavoriteDog set as null type adapter Person convertToString should generate a json with both one and many rel as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null,
        )

        val model = PersonModel(
            data = person,
        )

        val response = Fixtures.PERSON_ONE_AND_MANY_REL_AS_NULL

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }

    @Test
    fun `given a Person with allMyDogs with id set type adapter Person convertToString should generate a json with allMyDogs many rel and correct id set for each dog in both included and relationship blocks`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1).apply { setId("1") }, Dog(name = "Bongo", age = 2).apply { setId("2") }),
            myFavoriteDog = null,
        )

        val model = PersonModel(
            data = person,
        )

        val response = Fixtures.PERSON_ALL_MY_DOGS_WITH_ID_SET_FOR_EACH_DOG

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }

    @Test
    fun `given that a response that has a root meta should generate a Person with PersonRootMeta`() {
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

        val response = Fixtures.PERSON_WITH_ROOT_META

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model.rootMeta,
            result?.rootMeta,
        )
    }

    @Test
    fun `given that a response that has a resource object meta should generate a Person with PersonResourceMeta`() {
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

        val response = Fixtures.PERSON_WITH_RESOURCE_META

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model.resourceObjectMeta,
            result?.resourceObjectMeta,
        )
    }

    @Test
    fun `given that a response that has a one relationship meta should generate a Person with PersonRelationshipsMeta`() {
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
            relationshipsMeta = mapOf("myFavoriteDog" to relationshipMeta),
        )

        val response = Fixtures.PERSON_WITH_ONE_REL_META

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model.relationshipsMeta,
            result?.relationshipsMeta,
        )
    }

    @Test
    fun `given that a response that has multiple relationship meta should generate a Person with multiple PersonRelationshipsMeta`() {
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
            ),
        )

        val response = Fixtures.PERSON_WITH_MANY_REL_META

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model.relationshipsMeta,
            result?.relationshipsMeta,
        )
    }

    @Test
    fun `given that a response that has multiple  all types of meta should generate a Person with all types of meta`() {
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
            ),
        )

        val response = Fixtures.PERSON_WITH_ALL_METAS

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model,
            result,
        )
    }

    @Test
    fun `given that a response that has multiple  all types of links should generate a Person with all types of links`() {
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
            ),
        )

        val response = Fixtures.PERSON_WITH_ALL_LINKS

        val result = typeAdapter?.convertFromString(input = response)

        assertEquals(
            model,
            result,
        )
    }

    @Test
    fun `given that Person with all types of meta should generate a a response that has all types of meta`() {
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
            ),
        )

        val response = Fixtures.PERSON_WITH_ALL_META_TYPES_ENCODE

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }

    @Test
    fun `given that Person with all types of links should generate a a response that has all types of meta`() {
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
            ),
        )

        val response = Fixtures.PERSON_WITH_ALL_LINKS_TYPES_ENCODE

        val result = typeAdapter?.convertToString(input = model)

        assertEquals(
            response,
            result,
        )
    }
}
