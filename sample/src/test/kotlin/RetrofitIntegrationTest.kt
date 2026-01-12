import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.DogModel
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import com.infinum.jsonapix.retrofit.JsonXConverterFactory
import com.infinum.jsonapix.retrofit.JsonXRequestBodyConverter
import com.infinum.jsonapix.retrofit.JsonXResponseBodyConverter
import java.io.InputStreamReader
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit

/**
 * Integration tests for Retrofit module components.
 * Tests the complete workflow of request/response body conversion with Retrofit.
 */
@Suppress("StringLiteralDuplication")
internal class RetrofitIntegrationTest {

    private lateinit var factory: TypeAdapterFactory
    private lateinit var converterFactory: JsonXConverterFactory

    @BeforeEach
    fun setup() {
        factory = TypeAdapterFactory()
        converterFactory = JsonXConverterFactory(factory)
    }

    @Test
    fun `given a JsonXConverterFactory when getting response body converter for PersonModel should return valid converter`() { // ktlint-disable max-line-length
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        val converter = converterFactory.responseBodyConverter(
            PersonModel::class.java,
            emptyArray(),
            retrofit,
        )

        assertNotNull(converter, "Response body converter should not be null")
        assertTrue(converter is JsonXResponseBodyConverter<*>, "Should be JsonXResponseBodyConverter")
    }

    @Test
    fun `given a JsonXConverterFactory when getting request body converter for PersonModel should return valid converter`() { // ktlint-disable max-line-length
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        val converter = converterFactory.requestBodyConverter(
            PersonModel::class.java,
            emptyArray(),
            emptyArray(),
            retrofit,
        )

        assertNotNull(converter, "Request body converter should not be null")
        assertTrue(converter is JsonXRequestBodyConverter<*>, "Should be JsonXRequestBodyConverter")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting valid JSON response body should return PersonModel`() { // ktlint-disable max-line-length
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Alice",
                        "surname": "Johnson",
                        "age": 28
                    }
                }
            }
        """.trimIndent()

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Converted result should not be null")
        assertEquals("Alice", result?.data?.name)
        assertEquals("Johnson", result?.data?.surname)
        assertEquals(28, result?.data?.age)
        assertEquals("1", result?.data?.id())
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting PersonList response should return list of persons`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = """
            {
                "data": [
                    {
                        "type": "person",
                        "id": "1",
                        "attributes": {
                            "name": "Bob",
                            "surname": "Smith",
                            "age": 30
                        }
                    },
                    {
                        "type": "person",
                        "id": "2",
                        "attributes": {
                            "name": "Carol",
                            "surname": "White",
                            "age": 25
                        }
                    }
                ]
            }
        """.trimIndent()

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Converted result should not be null")
        assertEquals(2, result?.data?.size)
        assertEquals("Bob", result?.data?.get(0)?.data?.name)
        assertEquals("Carol", result?.data?.get(1)?.data?.name)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting PersonModel should create valid request body`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(adapter!!)
        val person = Person(
            name = "Dave",
            surname = "Brown",
            age = 35,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(model)

        assertNotNull(requestBody, "Request body should not be null")
        assertEquals(MediaType.parse("application/json; charset=UTF-8"), requestBody?.contentType())
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting model should create valid JSON content`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(adapter!!)
        val person = Person(
            name = "Emma",
            surname = "Davis",
            age = 29,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(model)
        assertNotNull(requestBody)

        // Read the content of the request body
        val buffer = okio.Buffer()
        requestBody!!.writeTo(buffer)
        val content = buffer.readUtf8()

        assertTrue(content.contains("Emma"), "Request body should contain name")
        assertTrue(content.contains("Davis"), "Request body should contain surname")
        assertTrue(content.contains("29"), "Request body should contain age")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with relationships should include included resources`() { // ktlint-disable max-line-length
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = getFileAsString("person_one_and_many_rel.json")

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result?.data?.myFavoriteDog, "myFavoriteDog should be populated from included")
        assertNotNull(result?.data?.allMyDogs, "allMyDogs should be populated from included")
        assertEquals(2, result?.data?.allMyDogs?.size, "Should have 2 dogs")
        assertEquals("Bella", result?.data?.myFavoriteDog?.name)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting model with relationships should include relationship data`() { // ktlint-disable max-line-length
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(adapter!!)
        val dog1 = Dog(name = "Max", age = 3).apply { setId("1") }
        val dog2 = Dog(name = "Rex", age = 5).apply { setId("2") }
        val person = Person(
            name = "Frank",
            surname = "Miller",
            age = 40,
            allMyDogs = listOf(dog1, dog2),
            myFavoriteDog = dog1,
        ).apply { setId("10") }
        val model = PersonModel(data = person)

        val requestBody = converter.convert(model)
        assertNotNull(requestBody)

        val buffer = okio.Buffer()
        requestBody!!.writeTo(buffer)
        val content = buffer.readUtf8()

        assertTrue(content.contains("relationships"), "Should contain relationships")
        assertTrue(content.contains("myFavoriteDog"), "Should contain myFavoriteDog relationship")
        assertTrue(content.contains("allMyDogs"), "Should contain allMyDogs relationship")
    }

    @Test
    fun `given a Retrofit instance with JsonXConverterFactory when making multiple conversions should work correctly`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        // Test multiple different types
        val personConverter = converterFactory.responseBodyConverter(
            PersonModel::class.java,
            emptyArray(),
            retrofit,
        )
        val dogConverter = converterFactory.responseBodyConverter(
            DogModel::class.java,
            emptyArray(),
            retrofit,
        )

        assertNotNull(personConverter, "Person converter should not be null")
        assertNotNull(dogConverter, "Dog converter should not be null")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with links should preserve links`() { // ktlint-disable max-line-length
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = """
            {
                "data": {
                    "type": "person",
                    "id": "1",
                    "attributes": {
                        "name": "Grace",
                        "surname": "Wilson",
                        "age": 27
                    },
                    "links": {
                        "self": "https://api.example.com/persons/1"
                    }
                },
                "links": {
                    "self": "https://api.example.com/persons/1"
                }
            }
        """.trimIndent()

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result?.rootLinks, "Root links should not be null")
        assertNotNull(result?.resourceObjectLinks, "Resource links should not be null")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with meta should preserve meta`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = getFileAsString("person_with_root_meta.json")

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result?.rootMeta, "Root meta should not be null")
    }

    @Test
    fun `given a JsonXConverterFactory when used in complete Retrofit setup should integrate seamlessly`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        // Verify both request and response converters can be created
        val requestConverter = converterFactory.requestBodyConverter(
            PersonModel::class.java,
            emptyArray(),
            emptyArray(),
            retrofit,
        )
        val responseConverter = converterFactory.responseBodyConverter(
            PersonModel::class.java,
            emptyArray(),
            retrofit,
        )

        assertNotNull(requestConverter, "Request converter should be created")
        assertNotNull(responseConverter, "Response converter should be created")

        // Test round-trip conversion
        val person = Person("Henry", "Taylor", 33, null, null)
        val model = PersonModel(data = person)

        @Suppress("UNCHECKED_CAST")
        val typedRequestConverter = requestConverter as JsonXRequestBodyConverter<PersonModel>
        val requestBody = typedRequestConverter.convert(model)
        assertNotNull(requestBody)

        // Extract the JSON from request body
        val buffer = okio.Buffer()
        requestBody!!.writeTo(buffer)
        val json = buffer.readUtf8()

        // Convert back using response converter
        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        @Suppress("UNCHECKED_CAST")
        val typedResponseConverter = responseConverter as JsonXResponseBodyConverter<PersonModel>
        val result = typedResponseConverter.convert(responseBody)

        assertNotNull(result)
        assertEquals("Henry", result?.data?.name)
        assertEquals("Taylor", result?.data?.surname)
        assertEquals(33, result?.data?.age)
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting empty data response should handle gracefully`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)
        val json = """
            {
                "data": []
            }
        """.trimIndent()

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Result should not be null")
        assertEquals(0, result?.data?.size, "Data should be empty list")
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting model with null relationships should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(adapter!!)
        val person = Person(
            name = "Ian",
            surname = "Anderson",
            age = 45,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(model)
        assertNotNull(requestBody)

        val buffer = okio.Buffer()
        requestBody!!.writeTo(buffer)
        val content = buffer.readUtf8()

        assertTrue(content.contains("Ian"), "Should contain person name")
        // Relationships with null values should be handled properly
    }

    @Test
    fun `given converters when handling large payloads should work efficiently`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(adapter!!)

        // Create a large JSON payload with many persons
        val persons = (1..100).joinToString(",") { i ->
            """
            {
                "type": "person",
                "id": "$i",
                "attributes": {
                    "name": "Person$i",
                    "surname": "Last$i",
                    "age": ${20 + i}
                }
            }
            """.trimIndent()
        }
        val json = """
            {
                "data": [$persons]
            }
        """.trimIndent()

        val responseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            json,
        )

        val result = converter.convert(responseBody)

        assertNotNull(result, "Result should not be null")
        assertEquals(100, result?.data?.size, "Should have 100 persons")
        assertEquals("Person1", result?.data?.get(0)?.data?.name)
        assertEquals("Person100", result?.data?.get(99)?.data?.name)
    }

    private fun getFileAsString(filename: String): String {
        val fileStream = javaClass.classLoader?.getResourceAsStream(filename)
        val fileReader: InputStreamReader? = fileStream?.reader()
        return fileReader?.readText() ?: ""
    }
}
