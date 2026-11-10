package com.infinum.jsonapix.processor.behavior

import com.infinum.jsonapix.asJsonXHttpException
import com.infinum.jsonapix.data.models.PersonalError
import com.infinum.jsonapix.format
import com.infinum.jsonapix.retrofit.JsonXHttpException
import com.infinum.jsonapix.retrofit.asJsonXHttpException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * Reading JSON:API errors from a Retrofit [HttpException], in both versions:
 * - the generated asJsonXHttpException(), for modules with Retrofit on their classpath;
 * - the hand-written asJsonXHttpException(format) from the retrofit artifact, for Android code
 *   whose models live in a multiplatform module.
 *
 * This file imports both, so it also proves they can be called side by side without ambiguity.
 * Every test checks that both return the same errors and keep the exception's response.
 */
internal class HttpExceptionIntegrationTest {

    @Test
    fun `given an error body with errors when reading the exception should return the errors`() {
        val body = """
            {
                "errors": [
                    { "desc": "Not found" },
                    { "desc": "Gone for good" }
                ]
            }
        """.trimIndent()

        assertBothVersionsReturn(
            listOf(PersonalError(desc = "Not found"), PersonalError(desc = "Gone for good")),
            body,
        )
    }

    @Test
    fun `given an error body with errors and other members when reading the exception should return the errors`() {
        val body = """
            {
                "errors": [
                    { "desc": "Not found", "status": "404" }
                ],
                "meta": { "requestId": "abc" }
            }
        """.trimIndent()

        assertBothVersionsReturn(listOf(PersonalError(desc = "Not found")), body)
    }

    @Test
    fun `given an empty errors array when reading the exception should return no errors`() {
        assertBothVersionsReturn(emptyList(), """{ "errors": [] }""")
    }

    @Test
    fun `given an empty error body when reading the exception should return no errors`() {
        assertBothVersionsReturn(emptyList(), "")
    }

    @Test
    fun `given an error body without errors when reading the exception should return no errors`() {
        assertBothVersionsReturn(emptyList(), """{ "meta": { "requestId": "abc" } }""")
    }

    @Test
    fun `given an error body that isn't JSON when reading the exception should return no errors`() {
        assertBothVersionsReturn(emptyList(), "<html><body>Bad gateway</body></html>")
    }

    @Test
    fun `given a response without an error body when reading the exception should return no errors`() {
        val generated = HttpException(Response.success("ok"))
        val handWritten = HttpException(Response.success("ok"))

        assertResult(emptyList(), generated, generated.asJsonXHttpException<PersonalError>())
        assertResult(emptyList(), handWritten, handWritten.asJsonXHttpException<PersonalError>(format))
    }

    // Reading the error body consumes it, so each version gets its own exception with the same body.
    private fun assertBothVersionsReturn(expected: List<PersonalError>, errorBody: String) {
        val generated = httpException(errorBody)
        val handWritten = httpException(errorBody)

        assertResult(expected, generated, generated.asJsonXHttpException<PersonalError>())
        assertResult(expected, handWritten, handWritten.asJsonXHttpException<PersonalError>(format))
    }

    private fun assertResult(
        expected: List<PersonalError>,
        exception: HttpException,
        result: JsonXHttpException<PersonalError>,
    ) {
        assertEquals(expected, result.errors)
        assertSame(exception.response(), result.response)
    }

    private fun httpException(errorBody: String): HttpException =
        HttpException(Response.error<Any>(404, errorBody.toResponseBody(JSON_API.toMediaType())))

    private companion object {
        const val JSON_API = "application/vnd.api+json"
    }
}
