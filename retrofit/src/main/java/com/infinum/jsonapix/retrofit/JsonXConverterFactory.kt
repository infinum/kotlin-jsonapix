package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.AdapterFactory
import java.lang.reflect.Type
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

class JsonXConverterFactory(private val adapterFactory: AdapterFactory) : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return if (type is Class<*>) {
            val adapter = adapterFactory.getAdapter(type.kotlin)
            adapter?.let {
                JsonXResponseBodyConverter(adapter)
            }
        } else {
            null
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return if (type is Class<*>) {
            val adapter = adapterFactory.getAdapter(type.kotlin)
            adapter?.let {
                JsonXRequestBodyConverter(it)
            }
        } else {
            null
        }
    }
}