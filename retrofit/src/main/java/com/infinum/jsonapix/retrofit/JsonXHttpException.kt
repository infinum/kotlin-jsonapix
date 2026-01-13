package com.infinum.jsonapix.retrofit

import com.infinum.jsonapix.core.resources.Error
import retrofit2.Response

data class JsonXHttpException<T : Error>(
    val response: Response<*>?,
    val errors: List<T>?,
)
