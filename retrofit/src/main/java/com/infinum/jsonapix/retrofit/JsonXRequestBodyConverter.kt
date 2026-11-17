package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.core.common.toJsonApiRequestDocument
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter

class JsonXRequestBodyConverter<Model> @JvmOverloads constructor(
    private val typeAdapter: TypeAdapter<Model>,
    private val mediaType: MediaType = JsonXMediaTypes.JSON,
) : Converter<Model, RequestBody> {

    // The body is handed over as bytes rather than as a String because String.toRequestBody
    // appends `charset=utf-8` to a media type that carries no charset, and JSON:API servers answer
    // 415 to a Content-Type of application/vnd.api+json with any media type parameter. Bytes send
    // the configured media type verbatim, encoded in the charset it declares so that the bytes and
    // the header agree. One that declares none, which every JSON:API media type does, gets UTF-8.
    override fun convert(value: Model): RequestBody? =
        typeAdapter.convertToString(value)
            .toJsonApiRequestDocument()
            .toByteArray(mediaType.charset() ?: Charsets.UTF_8)
            .toRequestBody(mediaType)
}
