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

    private fun getFileAsString(filename: String): String {
        val fileStream = javaClass.classLoader?.getResourceAsStream(filename)
        val fileReader: InputStreamReader? = fileStream?.reader()
        return fileReader?.readText() ?: ""
    }
}