import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonItem
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonRelationshipMeta
import com.infinum.jsonapix.data.models.PersonResourceMeta
import com.infinum.jsonapix.data.models.PersonRootMeta
import com.infinum.jsonapix.data.models.TypeAdapterList_Person
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.InputStreamReader

internal class TypeAdapterListTest {

    private var typeListAdapter: TypeAdapter<PersonList>? = null

    @BeforeEach
    fun setup() {
        typeListAdapter = TypeAdapterFactory().getAdapter(PersonList::class) as? TypeAdapterList_Person
    }

    @org.junit.jupiter.api.Test
    fun `given that response for both persons has all rels set type adapter Person list convertFromString should generate a Person class list with full rels on both list item Person`() {
        val items = listOf(
            PersonItem(
                data = Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship1.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship1.link.com"),
                ),
            ), PersonItem(
                data = Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bongo", age = 2)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship2.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship2.link.com"),
                ),
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_one_and_many_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that the response has an included block but first person not having allMyDogs rel type adapter Person convertFromString should generate a Person class list with first person allMyDogs as null and second person with full rels`() {
        val items = listOf(
            PersonItem(
                data = Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = Dog(name = "Bella", age = 1)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship1.link.com"),
                ),
            ),
            PersonItem(
                data = Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bongo", age = 2)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship2.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship2.link.com"),
                ),
            ),
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_first_person_empty_rel_second_person_full_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that the response has included block but second person has no allMyDogs rel type adapter Person convertFromString should generate a Person class list class with first person full rels and second person with allMyDogs as null`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship1.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship1.link.com"),
                ),
            ), PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = Dog(name = "Bongo", age = 2)
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship2.link.com"),
                ),
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_first_person_full_rel_second_person_no_many_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response has an included block but first person no allMyDogs rel and myFavoriteDog set as null type adapter Person convertFromString should generate a Person class list with first person allMyDogs and myFavoriteDog set as null`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
            ), PersonItem(
                Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bongo", age = 2)
                ), resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"), relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship2.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship2.link.com"),
                )
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_first_person_no_many_rels_null_one_rel_second_person_full_rels.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is no included block in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship1.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship1.link.com"),
                ),
            ),
            PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to DefaultLinks(self = "https://relationship2.link.com"),
                    "allMyDogs" to DefaultLinks(self = "https://relationship2.link.com"),
                ),
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )
        val response = getFileAsString("person_list_no_included_block.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but both persons have no rel in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
                relationshipsLinks = null,
                relationshipsMeta = null,
            ),
            PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
                relationshipsLinks = null,
                relationshipsMeta = null,
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_both_person_no_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but both persons have both rel set as null in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource1.link.com"),
            ),
            PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ),
                resourceObjectLinks = DefaultLinks(self = "https://resource2.link.com"),
            )
        )

        val personList = PersonList(
            data = items,
            rootLinks = DefaultLinks(self = "https://root.link.com"),
        )

        val response = getFileAsString("person_list_with_included_block_but_with_rel_values_set_as_null_for_both_persons.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but all link types in response are set as null type adapter Person list convertFromString should generate a valid Person list with no illegal argument exception`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1)
                )
            ), PersonItem(
                Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bongo", age = 2)
                )
            )
        )
        val personList = PersonList(
            data = items,
        )

        val response = getFileAsString("person_list_all_links_null.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList, result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is a null relationship data in response type adapter Person list convertFromString should throw an IllegalArgumentException`() {
        val response = getFileAsString("person_list_invalid_relationship_data.json")

        assertThrows<IllegalArgumentException> { typeListAdapter?.convertFromString(response) }
    }

    @org.junit.jupiter.api.Test
    fun `given that person list has allMyDogs and myFavoriteDog set type adapter Person list convertToString should generate a json with 2 persons having allMyDogs and myFavouriteDog relationships set with no links`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") }
                )
            ), PersonItem(
                Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = Dog(name = "Bongo", age = 2).apply { setId("0") }
                )
            )
        )
        val personList = PersonList(
            data = items,
        )

        val response = getFileAsString("person_list_convert_to_string_all_rels.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog rel`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") }
                )
            ),
            PersonItem(
                Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = null
                )
            )
        )

        val personList = PersonList(
            data = items
        )

        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog and empty allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and empty allMyDogs rels`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") }
                )
            ),
            PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = emptyList(), myFavoriteDog = null
                )
            )
        )

        val personList = PersonList(
            data = items
        )
        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog_empty_all_my_dogs.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog and empty allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and allMyDogs rels`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("0") },
                        Dog(name = "Bongo", age = 2).apply { setId("0") }),
                    myFavoriteDog = Dog(name = "Bella", age = 1).apply { setId("0") }
                ).apply { setId("0") }
            ), PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                ).apply { setId("0") }
            )
        )

        val personList = PersonList(
            data = items
        )
        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog_and_all_my_dogs.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list both persons has null myFavoriteDog and allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and allMyDogs rels`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                )
            ), PersonItem(
                Person(
                    name = "Jasminka", surname = "Apix", age = 28, allMyDogs = null, myFavoriteDog = null
                )
            )
        )

        val personList = PersonList(items)

        val response = getFileAsString("person_list_convert_to_string_both_persons_null_my_favorite_dog_and_all_my_dogs.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given a Person list with allMyDogs with id set type adapter list Person convertToString should generate a json with person array and allMyDogs many rel and correct id set for each dog in both included and relationship blocks`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("1") },
                        Dog(name = "Bongo", age = 2).apply { setId("2") }),
                    myFavoriteDog = null
                )
            ),
            PersonItem(
                Person(
                    name = "Jasminka",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(
                        Dog(name = "Bella", age = 1).apply { setId("1") },
                        Dog(name = "Bongo", age = 2).apply { setId("2") }),
                    myFavoriteDog = null
                )
            )
        )
        val personList = PersonList(items)

        val response = getFileAsString("person_list_all_my_dogs_with_id_set_for_each_dog.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }


    @Test
    fun `given a Person List with root meta should generate json a json with root meta info`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = null,
                    myFavoriteDog = null
                )
            )
        )
        val rootMeta = PersonRootMeta(owner = "Ali")
        val personList = PersonList(
            data = items,
            rootMeta = rootMeta,
        )
        val response = getFileAsString("person_list_convert_to_string_with_root_meta.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with resource object meta should generate json a json with resource meta info`() {
        val resourceMeta = PersonResourceMeta(writer = "Ali")

        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = null,
                    myFavoriteDog = null,
                ),
                resourceObjectMeta = resourceMeta
            )
        )
        val personList = PersonList(
            data = items,
        )
        val response = getFileAsString("person_list_convert_to_string_with_resource_meta.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with relationship meta should generate json a json with relationship meta info`() {
        val relationship1Meta = PersonRelationshipMeta("relation1")
        val relationship2Meta = PersonRelationshipMeta("relation2")

        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1),
                ),
                relationshipsMeta = mapOf(
                    "myFavoriteDog" to relationship1Meta,
                    "allMyDogs" to relationship2Meta,
                )
            )
        )
        val personList = PersonList(
            data = items,
        )
        val response = getFileAsString("person_list_convert_to_string_with_relationships_meta.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with all types of meta should generate json a json with all meta info`() {
        val relationship1Meta = PersonRelationshipMeta("relation1")
        val relationship2Meta = PersonRelationshipMeta("relation2")
        val resourceMeta = PersonResourceMeta(writer = "resource")

        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1),
                ),
                resourceObjectMeta = resourceMeta,
                relationshipsMeta = mapOf(
                    "myFavoriteDog" to relationship1Meta,
                    "allMyDogs" to relationship2Meta,
                )
            )
        )

        val rootMeta = PersonRootMeta(owner = "root")

        val personList = PersonList(
            data = items,
            rootMeta = rootMeta,
        )
        val response = getFileAsString("person_list_convert_to_string_with_all_meta.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with root links should generate json a json with root links info`() {
        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = null,
                    myFavoriteDog = null
                )
            )
        )
        val rootLinks = DefaultLinks(self = "root")
        val personList = PersonList(
            data = items,
            rootLinks = rootLinks,
        )
        val response = getFileAsString("person_list_convert_to_string_with_root_links.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with resource links should generate json a json with resource links info`() {
        val resourceLinks = DefaultLinks(self = "resource")

        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = null,
                    myFavoriteDog = null
                ),
                resourceObjectLinks = resourceLinks,
            )
        )
        val personList = PersonList(
            data = items,
        )
        val response = getFileAsString("person_list_convert_to_string_with_resource_links.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @Test
    fun `given a Person List with relationship links should generate json a json with links meta info`() {
        val relationship1Links = DefaultLinks("relation1")
        val relationship2Links = DefaultLinks("relation2")

        val items = listOf(
            PersonItem(
                Person(
                    name = "Jason",
                    surname = "Apix",
                    age = 28,
                    allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
                    myFavoriteDog = Dog(name = "Bella", age = 1),
                ),
                relationshipsLinks = mapOf(
                    "myFavoriteDog" to relationship1Links,
                    "allMyDogs" to relationship2Links,
                )
            )
        )
        val personList = PersonList(
            data = items,
        )
        val response = getFileAsString("person_list_convert_to_string_with_relationships_links.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given an empty Person list convertFromString should generate no error`() {
        val response = getFileAsString("person_list_blank_data.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            result?.data?.isEmpty(),
            true
        )
    }

    @org.junit.jupiter.api.Test
    fun `given an empty Person list convertToString should generate no error`() {
        val personList = PersonList(data = emptyList())
        val response = getFileAsString("person_list_blank_data_encode.json")

        val result = typeListAdapter?.convertToString(personList)

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
