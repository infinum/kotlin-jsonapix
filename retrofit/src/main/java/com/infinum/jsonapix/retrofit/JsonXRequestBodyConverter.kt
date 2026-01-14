package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.TypeAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter

class JsonXRequestBodyConverter<Model>(private val typeAdapter: TypeAdapter<Model>) : Converter<Model, RequestBody> {

    override fun convert(value: Model): RequestBody? =
        typeAdapter.convertToString(value).toRequestBody(MEDIA_TYPE)

    companion object {
        private val MEDIA_TYPE = "application/json; charset=UTF-8".toMediaType()
    }
}
