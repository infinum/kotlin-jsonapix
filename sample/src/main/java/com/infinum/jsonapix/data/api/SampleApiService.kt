package com.infinum.jsonapix.data.api

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import com.infinum.jsonapix.data.models.EmptyDataModel
import com.infinum.jsonapix.data.models.PersonList
import com.infinum.jsonapix.data.models.PersonModel
import retrofit2.http.GET

interface SampleApiService {

    @Mock
    @MockResponse(body = "responses/person.json")
    @GET("/person")
    suspend fun fetchPerson(): PersonModel

    @Mock
    @MockResponse(body = "responses/person_list.json")
    @GET("/persons")
    suspend fun fetchPersons(): PersonList

    @Mock
    @MockResponse(body = "responses/person_list_no_relationships.json")
    @GET("/personsNoRel")
    suspend fun fetchPersonsNoRelationships(): PersonList

    @Mock
    @MockResponse(body = "responses/error.json", code = 400, message = "ERROR")
    @GET("/error")
    suspend fun fetchError(): PersonModel

    @Mock
    @MockResponse(body = "responses/empty_resoruce_object.json")
    @GET("/empty")
    suspend fun fetchEmptyResponse(): EmptyDataModel
}
