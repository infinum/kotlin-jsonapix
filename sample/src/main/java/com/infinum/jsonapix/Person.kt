package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JsonApiX("person")
data class Person(
    @SerialName("mName") val name: String,
    val surname: String?,
    val age: Int,
    val dog: Dog,
    val sample: Sample,
    val listOfDogs: List<Dog>,
    val listOfInts: List<Int>
)
