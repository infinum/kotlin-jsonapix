package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Errors
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Reads the JSON:API errors from this exception's error body into a [JsonXHttpException].
 *
 * It's for Android code whose models live in a Kotlin Multiplatform module. JsonApiX generates
 * no Retrofit code into a multiplatform module, so the generated `asJsonXHttpException()`
 * without arguments doesn't exist there. Pass the `format` that JsonApiX generated in that module:
 *
 * ```
 * val errors = exception.asJsonXHttpException<PersonalError>(format).errors
 * ```
 *
 * It returns the same as the generated version. When the response has no error body, or the body
 * isn't a JSON:API document with errors of type [Model], `errors` is an empty list.
 *
 * @param Model the error model, an [Error] annotated with `@JsonApiXError`.
 * @param format the `format` that JsonApiX generated in the module that declares [Model].
 */
@Suppress("SwallowedException")
inline fun <reified Model : Error> HttpException.asJsonXHttpException(format: Json): JsonXHttpException<Model> {
    val response = response()
    val body = response?.errorBody()?.charStream()?.readText()
    val errors = if (body != null) {
        try {
            format.decodeFromString<Errors<Model>>(body).errors
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    } else {
        emptyList()
    }
    return JsonXHttpException(response, errors)
}
