package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.TypeAdapter
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

class JsonXRequestBodyConverter<Model>(private val typeAdapter: TypeAdapter<Model>) : Converter<Model, RequestBody> {

    companion object {
        private val MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8")
    }

    override fun convert(value: Model): RequestBody? {
        return RequestBody.create(MEDIA_TYPE, typeAdapter.convertToString(value))
    }
}
