package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.TypeAdapter
import okhttp3.ResponseBody
import retrofit2.Converter

class JsonXResponseBodyConverter<Model>(private val typeAdapter: TypeAdapter<Model>) : Converter<ResponseBody, Model> {
    override fun convert(value: ResponseBody): Model? = typeAdapter.convertFromString(value.string())
}
