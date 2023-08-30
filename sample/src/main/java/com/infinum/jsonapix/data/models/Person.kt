package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.JsonApiXError
import com.infinum.jsonapix.annotations.JsonApiXMeta
import com.infinum.jsonapix.annotations.MetaPlacementStrategy
import com.infinum.jsonapix.core.JsonApiModel
import com.infinum.jsonapix.core.resources.Meta
import kotlinx.serialization.Serializable

@Serializable
@JsonApiX("person")
data class Person(
    val name: String?,
    val surname: String,
    val age: Int,
    @HasMany("dog")
    val allMyDogs: List<Dog>?,
    @HasOne("dog")
    val myFavoriteDog: Dog? = null
) : JsonApiModel()



@Serializable
@JsonApiXMeta("person")
data class PersonRootMeta(val owner: String) : Meta

@Serializable
@JsonApiXMeta("person", MetaPlacementStrategy.DATA)
data class PersonResourceMeta(val writer: String) : Meta

@Serializable
@JsonApiXMeta("person", MetaPlacementStrategy.RELATIONSHIPS)
data class PersonRelationshipMeta(val user: String) : Meta

@Serializable
@JsonApiXError("person")
data class PersonalError(val desc: String) : com.infinum.jsonapix.core.resources.Error
