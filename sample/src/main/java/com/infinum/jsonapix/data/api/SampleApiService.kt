package com.infinum.jsonapix.data.api

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import com.infinum.jsonapix.data.models.Person
import retrofit2.http.GET

interface SampleApiService {

    @Mock
    @MockResponse(body = "responses/person.json")
    @GET("/person")
    suspend fun getPerson(): Person
}