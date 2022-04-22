# **JsonApiX**

- JSON API X is a Kotlin multiplatform, annotation processor library
- Implements a parser between Kotlin classes and JSON API specification strings in both directions
- Includes Retrofit module for easy API implementations


### List of libraries used in the project


This chapter lists all the libraries that make a bigger contribution to the project. It is essential for any future contributor to get familiar with them, before starting any work on the project. For that reason, links to the documentation are a part of the list.

 - Kotlinx serialization - [kotlinx.serialization/serialization-guide.md](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
 - Kotlin poet - https://square.github.io/kotlinpoet/

Along with the libraries documentation, the developer should also know his way around the JSON API, so here is a link to the [specification](https://jsonapi.org/).



## Getting started


Firstly, make sure to include `mavenCentral()` in your buildscript and add the serialization plugin:

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
    implementation("com.infinum.jsonapix:core:1.0.0-alpha")
    kapt("com.infinum.jsonapix:processor:1.0.0-alpha")

    // Optional: For Retrofit support
    implementation("com.infinum.jsonapix:retrofit:1.0.0-alpha")
```



## Usage


Let's take the example of a class named `Person`. 

```kotlin
@Serializable
@JsonApiX(type = "person")
data class Person(
    val firstName: String,
    val lastName: String,
    val age: Int,
    @HasMany(type = "friend")
    val friends: List<Friend>,
    @HasOne(type = "dog")
    val dog: Dog?
)
```

`@JsonApiX` annotation takes a String type parameter and it
must be combined with `@Serializable` from KotlinX Serialization.
Type is later used as a value for the `type` key in the JSON API specification.

`@HasOne` and `@HasMany` indicate the class relationships. They also take the type parameter,
that must be the same as the one indicated in the class definition.

In this case, `Dog` must also be annotated with `@Serializable` and `@JsonApiX(type = "dog")`, where `"dog"` is the type defined for the `Dog` class.

All non-primitive fields of a class should be some kind of relationship.

Hit build and wrappers will be generated. You are now ready to serialize and deserialize your class in the form of JSON API string.

To access the serialization and deserialization features, you need to use the `TypeAdapter` interface.
To get the type adapter for your specific class, use the generated `TypeAdapterFactory`:

```kotlin
// Gets adapter for a single instance of Person
val adapter = TypeAdapterFactory().getAdapter(Person::class)

// Gets adapter for a list of Person instances
val listAdapter = TypeAdapterFactory().getListAdapter(Person::class)

adapter.convertToString(person) // Produces JSON API String from a Person instance
adapter.convertFromString(inputJsonString) // Produces Person instance from JSON API String

listAdapter.convertToString(person) // Produces JSON API String from a Person list
listAdapter.convertFromString(inputJsonString) // Produces Person list from JSON API String
```

### Nullability


JsonApiX relies on the `included` JSON array from the JSON API specification when deserializing relationships.
If the input JSON API string is missing the data of a non-nullable relationship in the `included` array, 
deserialization attempt will throw a `JsonApiXMissingArgumentException`.
On the other hand, nullable relationships will be evaluated as `null`.

## Retrofit


To enable Retrofit support, you need to add our custom converter in your retrofit builder. 
It takes an instance of `TypeAdapterFactory` as a parameter.
No additional steps needed.

```kotlin
Retrofit.Builder()
     .addConverterFactory(JsonXConverterFactory(TypeAdapterFactory()))
     .baseUrl("https://www.example.com")
     .build()
```


## Contributing

Feedback and code contributions are very much welcome. Just make a pull request with a short description of your changes. By making contributions to this project you give permission for your code to be used under the same [license](LICENSE).


## License

```
Copyright 2021 Infinum

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Credits

Maintained and sponsored by [Infinum](http://www.infinum.com).
