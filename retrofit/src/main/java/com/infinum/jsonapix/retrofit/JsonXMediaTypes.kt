package com.infinum.jsonapix.retrofit

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

/**
 * Media types a request body can be sent as.
 */
object JsonXMediaTypes {

    /**
     * What the converter has always sent, and still sends unless told otherwise.
     */
    @JvmField
    val JSON: MediaType = "application/json; charset=UTF-8".toMediaType()

    /**
     * The media type the JSON:API specification defines. Servers are free to answer 415 to
     * anything else, so prefer this one. It carries no media type parameter on purpose: the
     * specification has servers answer 415 to this media type with any parameter, a charset
     * included.
     */
    @JvmField
    val JSON_API: MediaType = "application/vnd.api+json".toMediaType()
}
