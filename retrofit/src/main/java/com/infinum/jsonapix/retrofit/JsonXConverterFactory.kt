package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.adapters.AdapterFactory
import java.lang.reflect.Type
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * `requestMediaType` is the Content-Type request bodies are sent as. The default is what the
 * converter has always sent. Pass [JsonXMediaTypes.JSON_API] to send the media type the JSON:API
 * specification defines, which strict servers require.
 */
class JsonXConverterFactory @JvmOverloads constructor(
    private val adapterFactory: AdapterFactory,
    private val requestMediaType: MediaType = JsonXMediaTypes.JSON,
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? =
        when (type) {
            is Class<*> -> adapterFactory.getAdapter(type.kotlin)?.let {
                JsonXResponseBodyConverter(it)
            }
            else -> null
        }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? =
        when (type) {
            is Class<*> -> adapterFactory.getAdapter(type.kotlin)?.let {
                JsonXRequestBodyConverter(it, requestMediaType)
            }
            else -> null
        }
}
