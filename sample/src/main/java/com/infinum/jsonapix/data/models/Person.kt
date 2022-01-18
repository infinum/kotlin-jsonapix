package com.infinum.jsonapix.data.models

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.annotations.Links
import com.infinum.jsonapix.annotations.LinksPlacementStrategy
import com.infinum.jsonapix.core.JsonApiModel
import kotlinx.serialization.Serializable

@Serializable
@JsonApiX("person")
data class Person(
    val name: String,
    val surname: String,
    val age: Int,
    @HasMany("dog")
    val allMyDogs: List<Dog>,
    @HasOne("dog")
    val myFavoriteDog: Dog
) : JsonApiModel()

@Links("person", LinksPlacementStrategy.RELATIONSHIPS)
@Serializable
data class PersonLinks(val someData: String): com.infinum.jsonapix.core.resources.Links

// TODO To be moved to JSON files in assets
val personWithMissingDogTestJsonString = """
    {
        "errors":null,
        "data":{
            "type":"person",
            "id":"1",
            "attributes":{
                "age":28,
                "name":"Stef",
                "surname":"Banek"
            },
            "relationships":{
                "myFavoriteDog":{
                    "data":{
                        "type":"dog",
                        "id":"1"
                    }
                },
                "allMyDogs":{
                    "data":[
                        { "type":"dog", "id":"2" },
                        { "type":"dog", "id":"3" }
                    ]
                }
            }
        },
        "included":[
            {
                "type":"dog",
                "id":"2",
                "attributes":{
                    "age":2,
                    "name":"Bongo"
                }
            },
            {
                "type":"dog",
                "id":"3",
                "attributes":{
                    "age":3,
                    "name":"Sonic"
                }
            }
        ]
    }
""".trimIndent()

val personWithWrongTypeTestJsonString = """
    {
        "errors":null,
        "data":{
            "type":"dog",
            "id":"1",
            "attributes":{
                "age":28,
                "name":"Stef",
                "surname":"Banek"
            },
            "relationships":{
                "myFavoriteDog":{
                    "data":{
                        "type":"dog",
                        "id":"1"
                    }
                },
                "allMyDogs":{
                    "data":[
                        { "type":"dog", "id":"2" },
                        { "type":"dog", "id":"3" }
                    ]
                }
            }
        },
        "included":[
            {
                "type":"dog",
                "id":"1",
                "attributes":{
                    "age":1,
                    "name":"Bella"
                }
            },
            {
                "type":"dog",
                "id":"2",
                "attributes":{
                    "age":2,
                    "name":"Bongo"
                }
            },
            {
                "type":"dog",
                "id":"3",
                "attributes":{
                    "age":3,
                    "name":"Sonic"
                }
            }
        ]
    }
""".trimIndent()

val personListTestJsonString = """
    {
        "errors":null,
        "data":[{
            "type":"person",
            "id":"1",
            "attributes":{
                "age":28,
                "name":"Stef",
                "surname":"Banek"
            },
            "relationships":{
                "myFavoriteDog":{
                    "data":{
                        "type":"dog",
                        "id":"1"
                    }
                },
                "allMyDogs":{
                    "data":[
                        { "type":"dog", "id":"2" },
                        { "type":"dog", "id":"3" }
                    ]
                }
            }
        },
        {
            "type":"person",
            "id":"2",
            "attributes":{
                "age":26,
                "name":"Filip",
                "surname":"Floreani"
            },
            "relationships":{
                "myFavoriteDog":{
                    "data":{
                        "type":"dog",
                        "id":"3"
                    }
                },
                "allMyDogs":{
                    "data":[
                        { "type":"dog", "id":"1" },
                        { "type":"dog", "id":"3" }
                    ]
                }
            }
        }
        ],
        "included":[
            {
                "type":"dog",
                "id":"1",
                "attributes":{
                    "age":1,
                    "name":"Bella"
                }
            },
            {
                "type":"dog",
                "id":"2",
                "attributes":{
                    "age":2,
                    "name":"Bongo"
                }
            },
            {
                "type":"dog",
                "id":"3",
                "attributes":{
                    "age":3,
                    "name":"Sonic"
                }
            }
        ]
    }
""".trimIndent()
