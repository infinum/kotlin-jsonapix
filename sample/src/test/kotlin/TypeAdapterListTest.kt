import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getListAdapter
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.io.InputStreamReader
import kotlin.IllegalArgumentException

internal class TypeAdapterListTest {

    private var typeListAdapter: TypeAdapter<List<Person>>? = null

    @BeforeEach
    fun setup() {
        typeListAdapter = TypeAdapterFactory().getListAdapter()
    }

    @org.junit.jupiter.api.Test
    fun `given that response for both persons has all rels set type adapter Person list convertFromString should generate a Person class list with full rels on both list item Person`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bongo", age = 2)
        ))

        val response = getFileAsString("person_list_one_and_many_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that the response has an included block but first person not having allMyDogs rel type adapter Person convertFromString should generate a Person class list with first person allMyDogs as null and second person with full rels`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bongo", age = 2)
        ))

        val response = getFileAsString("person_list_first_person_empty_rel_second_person_full_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that the response has included block but second person has no allMyDogs rel type adapter Person convertFromString should generate a Person class list class with first person full rels and second person with allMyDogs as null`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = Dog(name = "Bongo", age = 2)
        ))

        val response = getFileAsString("person_list_first_person_full_rel_second_person_no_many_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that response has an included block but first person no allMyDogs rel and myFavoriteDog set as null type adapter Person convertFromString should generate a Person class list with first person allMyDogs and myFavoriteDog set as null`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bongo", age = 2)
        ))

        val response = getFileAsString("person_list_first_person_no_many_rels_null_one_rel_second_person_full_rels.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is no included block in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_no_included_block.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but both persons have no rel in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_both_person_no_rel.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but both persons have both rel set as null in response type adapter Person list convertFromString should generate a Person class list with allMyDogs null and myFavoriteDog null for both Persons`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_with_included_block_but_with_rel_values_set_as_null_for_both_persons.json")

        val result = typeListAdapter?.convertFromString(response)

        Assertions.assertEquals(
            personList,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is an included block but both persons have both links set as null in response type adapter Person list convertFromString should throw an IllegalArgumentException`() {
        val response = getFileAsString("person_list_with_both_person_links_set_as_null.json")

        assertThrows<IllegalArgumentException> { typeListAdapter?.convertFromString(response) }
    }

    @org.junit.jupiter.api.Test
    fun `type adapter Person list convertToString should generate a json with 2 persons having allMyDogs and myFavouriteDog relationships set with no links`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bongo", age = 2)
        ))

        val response = getFileAsString("person_list_convert_to_string_all_rels.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog rel`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog and empty allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and empty allMyDogs rels`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = emptyList(),
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog_empty_all_my_dogs.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list second person has null myFavoriteDog and empty allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and allMyDogs rels`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_convert_to_string_second_person_null_my_favorite_dog_and_all_my_dogs.json")

        val result = typeListAdapter?.convertToString(personList)

        Assertions.assertEquals(
            response,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that person list both persons has null myFavoriteDog and allMyDogs type adapter Person list convertToString should generate a json with first person having allMyDogs relationships myFavouriteDog relationships and second person with null myFavoriteDog and allMyDogs rels`() {
        val personList = listOf(Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ), Person(
            name = "Jasminka",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        ))

        val response = getFileAsString("person_list_convert_to_string_both_persons_null_my_favorite_dog_and_all_my_dogs.json")

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
