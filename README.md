# **Json API X**


### Introduction


JsonX is a Kotlin multiplatform, annotation processor library that was made to transform regular Kotlin classes into their JSON API representations, with the ability to serialize or deserialize them to or from strings.

Includes Retrofit module for easy API implementations. 



### List of libraries used in the project


This chapter lists all the libraries that make a bigger contribution to the project. It is essential for any future developer to get familiar with them, before starting any work on the project. For that reason, links to the documentation are a part of the list.

 - Kotlinx serialization - [kotlinx.serialization/serialization-guide.md](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
 - Kolin poet - https://square.github.io/kotlinpoet/

Along with the libraries documentation, the developer should also know his way around the JSON API, so here is a link to the [specification](https://jsonapi.org/).



### Setup


First make sure to include `mavenCentral()` in your buildscript and add the serialization plugin:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    
    dependencies {
         classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}
```

Then add the following dependencies and enable `kapt` and `kotlinx-serialization` plugins: 

```groovy
plugins {
    id "kotlinx-serialization"
    id "kotlin-kapt"
}
```

```groovy
    // Serialization API. Check the docs link above for newer versions
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
   
    // Json API X
    implementation("com.infinum.jsonapix:core:0.0.1")
    kapt("com.infinum.jsonapix:processor:0.0.1")

    // Optional: For Retrofit support
    implementation("com.infinum.jsonapix:retrofit:0.0.1")
```



### Usage


Let's take the example of a class named `Person`. `@JsonApiX` annotation takes a String type parameter and
must be combined with `@Serializable` from KotlinX Serialization. 
Type is later used as a value for the `type` key in the JSON API specification.
`@HasOne` and `@HasMany` indicate the class relationships. They also take the type parameter, 
that must be the same as the one indicated in the class definition. 
In this case, `Dog` must also be annotated with `@Serializable` and `@JsonApiX(type = "dog")`.
All non-primitive fields of a class should be some kind of relationship.

```kotlin
@Serializable
@JsonApiX(type = "person")
data class Person(
    val name: String,
    val surname: String,
    val age: Int,
    @HasMany(type = "friend")
    val friends: List<Person>,
    @HasOne(type = "dog")
    val dog: Dog
)
```

Hit build and wrappers will be generated. You are now ready to serialize and deserialize your class in the form of JSON API string.
To access the serialization and deserialization features, you neeed to use the `TypeAdapter` interface.
To get the type adapter for your specific class, use the generated `TypeAdapterFactory`:

```kotlin
val adapter = TypeAdapterFactory().getAdapter("qualified.name.of.your.class")

adapter.convertToString(person) // Produces JSON API String from a Person instance

adapter.convertFromString(inputJsonString) // Produces Person instance from JSON API String
```



### Retrofit


To enable Retrofit support, you need to add our custom converter in your retrofit builder. 
It takes an instance of `TypeAdapterFactory` as a parameter.
No additional steps needed.

```kotlin
Retrofit.Builder()
     .addConverterFactory(JsonXConverterFactory(TypeAdapterFactory()))
     .baseUrl("https://www.example.com")
     .build()
```
