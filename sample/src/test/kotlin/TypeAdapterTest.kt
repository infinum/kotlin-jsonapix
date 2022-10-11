import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.io.InputStreamReader

internal class TypeAdapterTest {

    private var typeAdapter: TypeAdapter<Person>? = null

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

        val response = getFileAsString("person_one_and_many_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
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

        val response = getFileAsString("person_no_included_block.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
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

        val response = getFileAsString("person_one_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
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

        val response = getFileAsString("person_many_rel_null_with_included.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
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

        val response = getFileAsString("person_many_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
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

        val response = getFileAsString("person_all_types_of_links_null.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `given that there is a null data of person in response type adapter Person convertFromString should throw an IllegalArgumentException`() {
        val response = getFileAsString("person_invalid_data.json")

        assertThrows<IllegalArgumentException> { typeAdapter?.convertFromString(response) }
    }

    @org.junit.jupiter.api.Test
    fun `given that there is a null relationship data in response type adapter Person convertFromString should throw an IllegalArgumentException`() {
        val response = getFileAsString("person_invalid_relationship_data.json")

        assertThrows<IllegalArgumentException> { typeAdapter?.convertFromString(response) }
    }

    @org.junit.jupiter.api.Test
    fun `given that person has both allMyDogs and myFavoriteDog set type adapter Person convertToString should generate a json with many allMyDogs relationships and myFavouriteDog relationship but links as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = listOf(Dog(name = "Bella", age = 1), Dog(name = "Bongo", age = 2)),
            myFavoriteDog = Dog(name = "Bella", age = 1)
        )

        val response = getFileAsString("person_no_links_all_rel.json")

        val result = typeAdapter?.convertToString(person)

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

        val response = getFileAsString("person_one_rel_null_many_rel_empty.json")

        val result = typeAdapter?.convertToString(person)

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

        val response = getFileAsString("person_one_and_many_rel_as_null.json")

        val result = typeAdapter?.convertToString(person)

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

        val response = getFileAsString("person_all_my_dogs_with_id_set_for_each_dog.json")

        val result = typeAdapter?.convertToString(person)

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