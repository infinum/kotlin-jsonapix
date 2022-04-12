package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.AdapterFactory
import java.lang.reflect.Type
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType

class JsonXConverterFactory(private val adapterFactory: AdapterFactory) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? =
        when (type) {
            is Class<*> -> adapterFactory.getAdapter(type.kotlin)?.let {
                JsonXResponseBodyConverter(it)
            }
            is ParameterizedType -> {
                adapterFactory.getListAdapter((type.actualTypeArguments.first() as Class<*>).kotlin)?.let {
                    JsonXResponseBodyConverter(it)
                }
            }
            else -> null
        }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? =
        when (type) {
            is Class<*> -> adapterFactory.getAdapter(type.kotlin)?.let {
                JsonXRequestBodyConverter(it)
            }
            is ParameterizedType -> {
                adapterFactory.getListAdapter((type.actualTypeArguments.first() as Class<*>).kotlin)?.let {
                    JsonXRequestBodyConverter(it)
                }
            }
            else -> null
        }
}
