import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.Person
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.io.InputStreamReader

internal class TypeAdapterTest {

    private var typeAdapter: TypeAdapter<Person>? = null

    @BeforeEach
    fun setup() {
        typeAdapter = TypeAdapterFactory().getAdapter()
    }

    @org.junit.jupiter.api.Test
    fun `type adapter Person convertFromString should generate a Person class with allMyDogs many relationships and one myFavouriteDog relationship`() {
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
    fun `type adapter Person convertFromString should generate a Person class with no relationships`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
            myFavoriteDog = null
        )

        val response = getFileAsString("person_no_rel.json")

        val result = typeAdapter?.convertFromString(response)

        Assertions.assertEquals(
            person,
            result
        )
    }

    @org.junit.jupiter.api.Test
    fun `type adapter Person convertFromString should generate a Person class with one myFavoriteDog relationship and no many relationships`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = emptyList(),
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
    fun `type adapter Person convertFromString should generate a Person class with many allMyDogs relationships and no one myFavouriteDog relationship`() {
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
    fun `type adapter Person convertToString should generate a json with many allMyDogs relationships and no one myFavouriteDog relationship`() {
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
    fun `type adapter Person convertToString should generate a json with one relationship null and many as empty`() {
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
    fun `type adapter Person convertToString should generate a json with both one and many rel as null`() {
        val person = Person(
            name = "Jason",
            surname = "Apix",
            age = 28,
            allMyDogs = null,
        )

        val response = getFileAsString("person_one_and_many_rel_as_null.json")

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