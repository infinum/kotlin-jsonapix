import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.core.adapters.AdapterFactory
import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.adapters.getAdapter
import com.infinum.jsonapix.core.resources.Relationship
import com.infinum.jsonapix.data.models.Dog
import com.infinum.jsonapix.data.models.DogModel
import com.infinum.jsonapix.data.models.Owner
import com.infinum.jsonapix.data.models.OwnerModel
import com.infinum.jsonapix.data.models.ShelterModel
import com.infinum.jsonapix.data.models.Shelter
import com.infinum.jsonapix.data.models.OwnerList
import com.infinum.jsonapix.data.models.OwnerItem
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonItem
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import com.infinum.jsonapix.data.models.PersonRootMeta
import com.infinum.jsonapix.linkMyFavoriteDog
import com.infinum.jsonapix.retrofit.JsonXConverterFactory
import com.infinum.jsonapix.retrofit.JsonXMediaTypes
import com.infinum.jsonapix.retrofit.JsonXRequestBodyConverter
import com.infinum.jsonapix.retrofit.JsonXResponseBodyConverter
import java.io.InputStreamReader
import java.nio.charset.Charset
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
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
        converterFactory = JsonXConverterFactory(adapterFactory = factory)
    }

    @Test
    fun `given a JsonXConverterFactory when getting response body converter for PersonModel should return valid converter`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        val converter = converterFactory.responseBodyConverter(
            type = PersonModel::class.java,
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        assertNotNull(converter, "Response body converter should not be null")
        assertTrue(converter is JsonXResponseBodyConverter<*>, "Should be JsonXResponseBodyConverter")
    }

    @Test
    fun `given a JsonXConverterFactory when getting request body converter for PersonModel should return valid converter`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        val converter = converterFactory.requestBodyConverter(
            type = PersonModel::class.java,
            parameterAnnotations = emptyArray(),
            methodAnnotations = emptyArray(),
            retrofit = retrofit,
        )

        assertNotNull(converter, "Request body converter should not be null")
        assertTrue(converter is JsonXRequestBodyConverter<*>, "Should be JsonXRequestBodyConverter")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting valid JSON response body should return PersonModel`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
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

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

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

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
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

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

        assertNotNull(result, "Converted result should not be null")
        assertEquals(2, result?.data?.size)
        assertEquals("Bob", result?.data?.get(0)?.data?.name)
        assertEquals("Carol", result?.data?.get(1)?.data?.name)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting PersonModel should create valid request body`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Dave",
            surname = "Brown",
            age = 35,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(value = model)

        assertNotNull(requestBody, "Request body should not be null")
        assertEquals("application/json; charset=UTF-8".toMediaTypeOrNull(), requestBody?.contentType())
    }

    @Test
    fun `given a JsonXRequestBodyConverter built with the JSON API media type should send it with no charset`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!, mediaType = JsonXMediaTypes.JSON_API)
        val person = Person(name = "Dave", surname = "Brown", age = 35, allMyDogs = null, myFavoriteDog = null)
        val model = PersonModel(data = person)

        val requestBody = converter.convert(value = model)

        assertNotNull(requestBody, "Request body should not be null")
        assertEquals("application/vnd.api+json".toMediaTypeOrNull(), requestBody?.contentType())
    }

    @Test
    fun `given a JsonXRequestBodyConverter built with a media type that declares a charset should encode the body in it`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(
            typeAdapter = adapter!!,
            mediaType = "application/json; charset=ISO-8859-1".toMediaType(),
        )
        // One byte in ISO-8859-1 and two in UTF-8, so reading the body back as ISO-8859-1 only
        // yields this name if the body was written in the charset the media type declares.
        val name = "Jos\u00E9"
        val person = Person(name = name, surname = "Brown", age = 35, allMyDogs = null, myFavoriteDog = null)
        val model = PersonModel(data = person)

        val requestBody = converter.convert(value = model)

        assertTrue(
            requestBody.asString(charset = Charsets.ISO_8859_1).contains("\"name\":\"$name\""),
            "Body should be encoded in the charset the media type declares",
        )
    }

    // The media type parameter has a default, so Kotlin callers compile either way. These pin the
    // constructors that callers compiled against an earlier release, and Java callers, bind to.
    @Test
    fun `given the converters when the media type is not passed should still expose the single argument constructors`() {
        assertNotNull(
            JsonXConverterFactory::class.java.getConstructor(AdapterFactory::class.java),
            "JsonXConverterFactory(AdapterFactory) should still exist",
        )
        assertNotNull(
            JsonXRequestBodyConverter::class.java.getConstructor(TypeAdapter::class.java),
            "JsonXRequestBodyConverter(TypeAdapter) should still exist",
        )
    }

    @Test
    fun `given a JsonXConverterFactory built with a request media type should hand it to the request converter`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()
        val factoryWithMediaType = JsonXConverterFactory(
            adapterFactory = factory,
            requestMediaType = JsonXMediaTypes.JSON_API,
        )

        val converter = factoryWithMediaType.requestBodyConverter(
            type = PersonModel::class.java,
            parameterAnnotations = emptyArray(),
            methodAnnotations = emptyArray(),
            retrofit = retrofit,
        )

        @Suppress("UNCHECKED_CAST")
        val typedConverter = converter as JsonXRequestBodyConverter<PersonModel>
        val person = Person(name = "Dave", surname = "Brown", age = 35, allMyDogs = null, myFavoriteDog = null)
        val requestBody = typedConverter.convert(value = PersonModel(data = person))

        assertEquals("application/vnd.api+json".toMediaTypeOrNull(), requestBody?.contentType())
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting model should create valid JSON content`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Emma",
            surname = "Davis",
            age = 29,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(value = model)
        assertNotNull(requestBody)

        // Read the content of the request body
        val buffer = okio.Buffer()
        requestBody?.writeTo(buffer)
        val content = buffer.readUtf8()

        assertTrue(content.contains("Emma"), "Request body should contain name")
        assertTrue(content.contains("Davis"), "Request body should contain surname")
        assertTrue(content.contains("29"), "Request body should contain age")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with relationships should include included resources`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
        val json = getFileAsString(filename = "person_one_and_many_rel.json")

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result?.data?.myFavoriteDog, "myFavoriteDog should be populated from included")
        assertNotNull(result?.data?.allMyDogs, "allMyDogs should be populated from included")
        assertEquals(2, result?.data?.allMyDogs?.size, "Should have 2 dogs")
        assertEquals("Bella", result?.data?.myFavoriteDog?.name)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting a model with relationships should write a request document with no nulls and no included`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val dog1 = Dog(name = "Max", age = 3).apply { setId(id = "1") }
        val dog2 = Dog(name = "Rex", age = 5).apply { setId(id = "2") }
        val person = Person(
            name = "Frank",
            surname = "Miller",
            age = 40,
            allMyDogs = listOf(dog1, dog2),
            myFavoriteDog = dog1,
        ).apply { setId(id = "10") }
        val model = PersonModel(data = person)

        val content = converter.convert(value = model).asString()

        assertEquals(getFileAsString(filename = "person_request_body_with_relationships.json"), content)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting a model with no relationships should write a request document with no nulls and no included`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Frank",
            surname = "Miller",
            age = 40,
            allMyDogs = null,
            myFavoriteDog = null,
        ).apply { setId(id = "10") }
        val model = PersonModel(data = person)

        val content = converter.convert(value = model).asString()

        assertEquals(getFileAsString(filename = "person_request_body_no_relationships.json"), content)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when the model has a root meta should keep meta in the request document`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Frank",
            surname = "Miller",
            age = 40,
            allMyDogs = null,
            myFavoriteDog = null,
        ).apply { setId(id = "10") }
        val model = PersonModel(data = person, rootMeta = PersonRootMeta(owner = "Frank"))

        val content = converter.convert(value = model).asString()

        assertEquals(getFileAsString(filename = "person_request_body_with_root_meta.json"), content)
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting a list model should write a request document with no nulls and no included`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Frank",
            surname = "Miller",
            age = 40,
            allMyDogs = listOf(Dog(name = "Max", age = 3).apply { setId(id = "1") }),
            myFavoriteDog = null,
        ).apply { setId(id = "10") }
        val personList = PersonList(data = listOf(PersonItem(person)))

        val content = converter.convert(value = personList).asString()

        assertEquals(getFileAsString(filename = "person_list_request_body.json"), content)
    }

    @Test
    fun `given a Retrofit instance with JsonXConverterFactory when making multiple conversions should work correctly`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(converterFactory)
            .build()

        // Test multiple different types
        val personConverter = converterFactory.responseBodyConverter(
            type = PersonModel::class.java,
            annotations = emptyArray(),
            retrofit = retrofit,
        )
        val dogConverter = converterFactory.responseBodyConverter(
            type = DogModel::class.java,
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        assertNotNull(personConverter, "Person converter should not be null")
        assertNotNull(dogConverter, "Dog converter should not be null")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with links should preserve links`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
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

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result?.rootLinks, "Root links should not be null")
        assertNotNull(result?.resourceObjectLinks, "Resource links should not be null")
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting response with meta should preserve meta`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
        val json = getFileAsString(filename = "person_with_root_meta.json")

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

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
            type = PersonModel::class.java,
            parameterAnnotations = emptyArray(),
            methodAnnotations = emptyArray(),
            retrofit = retrofit,
        )
        val responseConverter = converterFactory.responseBodyConverter(
            type = PersonModel::class.java,
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        assertNotNull(requestConverter, "Request converter should be created")
        assertNotNull(responseConverter, "Response converter should be created")

        // Test round-trip conversion
        val person = Person(name = "Henry", surname = "Taylor", age = 33, allMyDogs = null, myFavoriteDog = null)
        val model = PersonModel(data = person)

        @Suppress("UNCHECKED_CAST")
        val typedRequestConverter = requestConverter as JsonXRequestBodyConverter<PersonModel>
        val requestBody = typedRequestConverter.convert(value = model)
        assertNotNull(requestBody)

        // Extract the JSON from request body
        val buffer = okio.Buffer()
        requestBody?.writeTo(buffer)
        val json = buffer.readUtf8()

        // Convert back using response converter
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        @Suppress("UNCHECKED_CAST")
        val typedResponseConverter = responseConverter as JsonXResponseBodyConverter<PersonModel>
        val result = typedResponseConverter.convert(value = responseBody)

        assertNotNull(result)
        assertEquals("Henry", result?.data?.name)
        assertEquals("Taylor", result?.data?.surname)
        assertEquals(33, result?.data?.age)
    }

    @Test
    fun `given a JsonXResponseBodyConverter when converting empty data response should handle gracefully`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)
        val json = """
            {
                "data": []
            }
        """.trimIndent()

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

        assertNotNull(result, "Result should not be null")
        assertEquals(0, result?.data?.size, "Data should be empty list")
    }

    @Test
    fun `given a JsonXRequestBodyConverter when converting model with null relationships should handle gracefully`() {
        val adapter = factory.getAdapter<PersonModel>()
        assertNotNull(adapter)

        val converter = JsonXRequestBodyConverter(typeAdapter = adapter!!)
        val person = Person(
            name = "Ian",
            surname = "Anderson",
            age = 45,
            allMyDogs = null,
            myFavoriteDog = null,
        )
        val model = PersonModel(data = person)

        val requestBody = converter.convert(value = model)
        assertNotNull(requestBody)

        val buffer = okio.Buffer()
        requestBody?.writeTo(buffer)
        val content = buffer.readUtf8()

        assertTrue(content.contains("Ian"), "Should contain person name")
        // Relationships with null values should be handled properly
    }

    @Test
    fun `given converters when handling large payloads should work efficiently`() {
        val adapter = factory.getAdapter<PersonList>()
        assertNotNull(adapter)

        val converter = JsonXResponseBodyConverter(typeAdapter = adapter!!)

        // Create a large JSON payload with many persons
        val persons = (1..100).joinToString(separator = ",") { i ->
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

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val result = converter.convert(value = responseBody)

        assertNotNull(result, "Result should not be null")
        assertEquals(100, result?.data?.size, "Should have 100 persons")
        assertEquals("Person1", result?.data?.get(0)?.data?.name)
        assertEquals("Person100", result?.data?.get(99)?.data?.name)
    }

    @Test
    fun `given a Relationship Unset when converting should leave the relationship member out`() {
        val converter = ownerRequestConverter()
        val owner = Owner(name = "Ana").apply { setId(id = "5") }

        val content = converter.convert(value = OwnerModel(data = owner)).asString()

        assertEquals(getFileAsString(filename = "owner_request_body_unset.json"), content)
    }

    @Test
    fun `given a Relationship Clear when converting should write a null data member`() {
        val converter = ownerRequestConverter()
        val owner = Owner(name = "Ana", myFavoriteDog = Relationship.Clear).apply { setId(id = "5") }

        val content = converter.convert(value = OwnerModel(data = owner)).asString()

        assertEquals(getFileAsString(filename = "owner_request_body_clear.json"), content)
    }

    @Test
    fun `given a Relationship To when converting should write linkage and no included`() {
        val converter = ownerRequestConverter()
        val dog = Dog(name = "Max", age = 3).apply { setId(id = "1") }
        val owner = Owner(name = "Ana", myFavoriteDog = Relationship.To(dog)).apply { setId(id = "5") }

        val content = converter.convert(value = OwnerModel(data = owner)).asString()

        assertEquals(getFileAsString(filename = "owner_request_body_to_resource.json"), content)
    }

    @Test
    fun `given a Relationship Linked when converting should write linkage for a resource it never fetched`() {
        val converter = ownerRequestConverter()
        val owner = Owner(name = "Ana").apply { setId(id = "5") }.linkMyFavoriteDog(id = "7")

        val content = converter.convert(value = OwnerModel(data = owner)).asString()

        assertEquals(getFileAsString(filename = "owner_request_body_linked_resource.json"), content)
    }

    @Test
    fun `given a response with linkage and a matching included resource should read a Relationship To`() {
        val json = """
            {
                "data": {
                    "type": "owner",
                    "id": "5",
                    "attributes": { "name": "Ana" },
                    "relationships": { "myFavoriteDog": { "data": { "type": "dog", "id": "1" } } }
                },
                "included": [
                    { "type": "dog", "id": "1", "attributes": { "name": "Max", "age": 3 } }
                ]
            }
        """.trimIndent()

        val result = ownerResponseConverter().convert(value = json.toResponseBody())

        val relationship = result?.data?.myFavoriteDog
        assertTrue(relationship is Relationship.To, "Expected To but was $relationship")
        assertEquals("Max", (relationship as Relationship.To).value.name)
    }

    @Test
    fun `given a response with linkage but no matching included resource should read a Relationship Linked`() {
        val json = """
            {
                "data": {
                    "type": "owner",
                    "id": "5",
                    "attributes": { "name": "Ana" },
                    "relationships": { "myFavoriteDog": { "data": { "type": "dog", "id": "1" } } }
                },
                "included": []
            }
        """.trimIndent()

        val result = ownerResponseConverter().convert(value = json.toResponseBody())

        val relationship = result?.data?.myFavoriteDog
        assertTrue(relationship is Relationship.Linked, "Expected Linked but was $relationship")
        assertEquals("1", (relationship as Relationship.Linked).id.id)
        assertEquals("dog", relationship.id.type)
    }

    @Test
    fun `given a response with no relationship member should read a Relationship Unset`() {
        val json = """
            {
                "data": { "type": "owner", "id": "5", "attributes": { "name": "Ana" } }
            }
        """.trimIndent()

        val result = ownerResponseConverter().convert(value = json.toResponseBody())

        assertEquals(Relationship.Unset, result?.data?.myFavoriteDog)
    }

    @Test
    fun `given a response whose relationship carries a null data should read a Relationship Unset`() {
        val json = """
            {
                "data": {
                    "type": "owner",
                    "id": "5",
                    "attributes": { "name": "Ana" },
                    "relationships": { "myFavoriteDog": { "data": null } }
                }
            }
        """.trimIndent()

        val result = ownerResponseConverter().convert(value = json.toResponseBody())

        // A response never asks a client to clear anything, and a relationship object that carries
        // only links or meta says nothing about the linkage. Reading either as Clear would turn an
        // ordinary response into a request that deletes the relationship on the next write.
        assertEquals(Relationship.Unset, result?.data?.myFavoriteDog)
    }

    @Test
    fun `given a Relationship Linked when round tripping through a request should keep pointing at the same resource`() {
        val json = """
            {
                "data": {
                    "type": "owner",
                    "id": "5",
                    "attributes": { "name": "Ana" },
                    "relationships": { "myFavoriteDog": { "data": { "type": "dog", "id": "7" } } }
                },
                "included": []
            }
        """.trimIndent()

        val read = ownerResponseConverter().convert(value = json.toResponseBody())
        val content = ownerRequestConverter().convert(value = OwnerModel(data = read!!.data)).asString()

        assertEquals(getFileAsString(filename = "owner_request_body_linked_resource.json"), content)
    }

    @Test
    fun `given a list whose items are in different relationship states should write each one`() {
        val listAdapter = requireNotNull(factory.getAdapter<OwnerList>())
        val converter = JsonXRequestBodyConverter(typeAdapter = listAdapter)
        val dog = Dog(name = "Max", age = 3).apply { setId(id = "1") }
        val list = OwnerList(
            data = listOf(
                OwnerItem(data = Owner(name = "Ana", myFavoriteDog = Relationship.To(dog)).apply { setId(id = "5") }),
                OwnerItem(data = Owner(name = "Bo", myFavoriteDog = Relationship.Clear).apply { setId(id = "6") }),
            ),
        )

        val content = converter.convert(value = list).asString()

        assertEquals(getFileAsString(filename = "owner_list_request_body_mixed_states.json"), content)
    }

    @Test
    fun `given a nullable Relationship property holding null should leave the member out`() {
        val converter = JsonXRequestBodyConverter(typeAdapter = requireNotNull(factory.getAdapter<ShelterModel>()))
        val shelter = Shelter(name = "Haven").apply { setId(id = "9") }

        val content = converter.convert(value = ShelterModel(data = shelter)).asString()

        // A Relationship<T>? property spells "leave this alone" twice, as null and as Unset.
        // Both read that way rather than failing to compile.
        assertEquals(getFileAsString(filename = "shelter_request_body_null_property.json"), content)
    }

    @Test
    fun `given a nullable Relationship property holding Clear should write a null data member`() {
        val converter = JsonXRequestBodyConverter(typeAdapter = requireNotNull(factory.getAdapter<ShelterModel>()))
        val shelter = Shelter(name = "Haven", featuredDog = Relationship.Clear).apply { setId(id = "9") }

        val content = converter.convert(value = ShelterModel(data = shelter)).asString()

        assertEquals(getFileAsString(filename = "shelter_request_body_clear.json"), content)
    }

    private fun ownerRequestConverter(): JsonXRequestBodyConverter<OwnerModel> =
        JsonXRequestBodyConverter(typeAdapter = requireNotNull(factory.getAdapter<OwnerModel>()))

    private fun ownerResponseConverter(): JsonXResponseBodyConverter<OwnerModel> =
        JsonXResponseBodyConverter(typeAdapter = requireNotNull(factory.getAdapter<OwnerModel>()))

    private fun String.toResponseBody(): ResponseBody =
        toResponseBody(contentType = "application/json".toMediaTypeOrNull())

    private fun RequestBody?.asString(charset: Charset = Charsets.UTF_8): String {
        val buffer = okio.Buffer()
        requireNotNull(this) { "Request body should not be null" }.writeTo(buffer)
        return buffer.readString(charset)
    }

    private fun getFileAsString(filename: String): String {
        val fileStream = javaClass.classLoader?.getResourceAsStream(filename)
        val fileReader: InputStreamReader? = fileStream?.reader()
        return fileReader?.readText() ?: ""
    }
}
