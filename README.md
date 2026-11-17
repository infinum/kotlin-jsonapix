![Maven Central Version](https://img.shields.io/maven-central/v/com.infinum.jsonapix/processor?style=plastic)


### <img align="left" src="logo.svg" width="48">
# JsonApiX

## Description

- JSON API X is an annotation processor library for Android and Kotlin Multiplatform
- Implements a parser between Kotlin classes and JSON API specification strings in both directions
- Includes Retrofit module for easy API implementations

## Table of contents

* [Requirements](#requirements)
* [Getting started](#getting-started)
  * [KSP setup](#ksp-setup)
  * [Kotlin Multiplatform setup](#kotlin-multiplatform-setup)
* [Usage](#usage)
* [JsonApiModel - Handling `links` and `meta` JSON API fields](#jsonapimodel---handling-links-and-meta-json-api-fields)
* [Retrofit Support](#retrofit-support)
  * [Models in a Kotlin Multiplatform module](#models-in-a-kotlin-multiplatform-module)
* [List of libraries used in the project](#list-of-libraries-used-in-the-project)
* [Contributing](#contributing)
* [License](#license)
* [Credits](#credits)

## Requirements

The library generates its code with an annotation processor, which runs on either KAPT or KSP.
Each has its own processor artifact, and each artifact goes on its own Gradle configuration:

| Processor | Gradle configuration | Artifact                             |
|-----------|----------------------|--------------------------------------|
| KAPT      | `kapt`               | `com.infinum.jsonapix:processor`     |
| KSP       | `ksp`                | `com.infinum.jsonapix:processor-ksp` |

An artifact on the other configuration generates nothing, so code that uses the generated classes doesn't compile.
All other artifacts are the same for both processors.

Both processors generate the same code, so you can move a module from one to the other by changing only its Gradle setup.

KSP support requires KSP2, version 2.3.10 or newer on Kotlin 2.4, and is tested with 2.3.12.

Kotlin Multiplatform modules need KSP. See [Kotlin Multiplatform setup](#kotlin-multiplatform-setup).

> [!WARNING]
> Apply only one of the two processors in a module. Applying both generates every class twice, and the build fails with duplicate declarations.

With KAPT, the setup is different on a Kotlin or Java project.

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
    id "kotlin-kapt" or id "com.android.legacy-kapt" -> depends on your AGP version
}
```

```groovy
// Serialization API. Check the docs link above for newer versions
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

// Json API X
implementation("com.infinum.jsonapix:core:<latest-version>")

// Kotlin project
kapt("com.infinum.jsonapix:processor:<latest-version>")

// Java project
annotationProcessor "com.infinum.jsonapix:processor:<latest-version>"

// Optional: For Retrofit support
implementation("com.infinum.jsonapix:retrofit:<latest-version>")

// Optional: Custom lint tool checker
implementation("com.infinum.jsonapix:lint:<latest-version>")


```

### KSP setup

Include `mavenCentral()` and the serialization plugin as shown above. Then enable the `ksp` plugin instead of `kapt`,
and put `processor-ksp` on the `ksp` configuration:

```groovy
plugins {
    id "kotlinx-serialization"
    id "com.google.devtools.ksp" version "2.3.12"
}
```

```groovy
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

// Json API X
implementation("com.infinum.jsonapix:core:<latest-version>")
ksp("com.infinum.jsonapix:processor-ksp:<latest-version>")
```

The optional lint checker is added the same way as with KAPT.

The `ksp` plugin works with the built-in Kotlin support in AGP 9. Other annotation processors, such as Hilt, can keep
running on KAPT in the same module.

To move a module from KAPT to KSP, apply the `ksp` plugin and replace the `kapt` line for `processor` with the `ksp`
line for `processor-ksp`. Your code doesn't change: the generated classes, their packages and their API stay the same.
If JsonApiX was the last processor on KAPT in that module, you can remove the KAPT plugin too.

### Kotlin Multiplatform setup

In a Kotlin Multiplatform (KMP) module, you declare your models in `commonMain`, and JsonApiX generates their code into
`commonMain` too. Encoding and decoding then work the same on every target, from common code.

`annotations` and `core` are multiplatform libraries, published for six targets:

- `jvm`, which Android uses too;
- `iosArm64` and `iosSimulatorArm64`;
- `js` and `wasmJs`, for the browser and Node.js;
- `linuxX64`.

Your module can declare any of them. A target that isn't on the list fails dependency resolution with "No matching
variant of com.infinum.jsonapix:core". If you need another target, [open an issue](CONTRIBUTING.md#issue-reporting).
Adding a target doesn't break existing users.

The other artifacts are JVM-only. This is where each artifact goes:

| Artifact        | Platforms     | Where it goes                                                                                          |
|-----------------|---------------|--------------------------------------------------------------------------------------------------------|
| `core`          | Multiplatform | `commonMain`. It brings `annotations` and `kotlinx-serialization-json` with it.                        |
| `annotations`   | Multiplatform | Nowhere, it comes with `core`.                                                                         |
| `processor-ksp` | JVM only      | `kspCommonMainMetadata`. It runs in your build, so it works for every target.                          |
| `processor`     | JVM only      | Not in KMP modules. It runs on KAPT, which can't process `commonMain`.                                 |
| `retrofit`      | JVM only      | Android code. See [Models in a Kotlin Multiplatform module](#models-in-a-kotlin-multiplatform-module). |
| `lint`          | JVM only      | Android modules.                                                                                       |

Apply the Kotlin Multiplatform, kotlinx-serialization and KSP plugins to the module. Then put `core` in `commonMain`,
put `processor-ksp` on `kspCommonMainMetadata`, add the generated code to `commonMain`'s sources, and make every
compilation wait for it. In the module's `build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

kotlin {
    // Your targets, from the list above
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            // The code JsonApiX generates for commonMain
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
            dependencies {
                api("com.infinum.jsonapix:core:<latest-version>")
            }
        }
    }
}

dependencies {
    // Only this KSP configuration, see below
    add("kspCommonMainMetadata", "com.infinum.jsonapix:processor-ksp:<latest-version>")
}

// Generate the code before anything compiles against it
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
// KSP also registers a task for every target, which reads commonMain's sources, the generated code included
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}
```

Apply the plugins the way your build already does, for example with versions from your version catalog. JsonApiX is
built with Kotlin 2.4.20, and its KMP setup is tested with KSP 2.3.12.

`core` is an `api` dependency because the generated classes, such as `TypeAdapterFactory` and `PersonModel`, use
`core`'s types in their public API. Every module that uses them, such as your Android app, needs `core` on its compile
classpath too.

Then use the generated classes from common code, as described in [Usage](#usage).

> [!WARNING]
> Put `processor-ksp` on `kspCommonMainMetadata` only. Configurations for single targets, such as `kspAndroid`,
> `kspJvm` or `kspIosArm64`, generate the code once more for those targets, and the build fails with "Redeclaration"
> and "Conflicting overloads" errors.

A few more rules apply:

- Declare your models in `commonMain`. JsonApiX doesn't process models in platform source sets, such as `androidMain`
  or `iosMain`.
- Use KSP. KAPT can't process `commonMain`, so `processor` stays for Android-only modules.
- Generate in one module per build, as on Android. The generated `TypeAdapterFactory`, `format` and the other shared
  files always use the package `com.infinum.jsonapix`, so two modules that generate them clash.

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
) : JsonApiModel()
```

`@JsonApiX` annotation takes a String type parameter and it must be combined with `@Serializable` from KotlinX Serialization. Type is later
used as a value for the `type` key in the JSON API specification. The class must also extend `JsonApiModel`.

`@HasOne` and `@HasMany` indicate the class relationships. They also take the type parameter, that must be the same as the one indicated in
the class definition.

In this case, `Dog` must also be annotated with `@Serializable` and `@JsonApiX(type = "dog")`, where `"dog"` is the type defined for
the `Dog` class, and extend `JsonApiModel`.

All non-primitive fields of a class should be some kind of relationship.

Once you hit build, `PersonModel`, `PersonItem`, and `PersonList` are automatically generated. These wrappers facilitate the serialization and deserialization of your classes into and from JSON API strings.

For example, for a single `Person` instance, `PersonModel` is generated as follows:
```kotlin
public data class PersonModel(
  public val `data`: Person, // The Person object
  // ... Root values
  // ... Resource object values
)
```
```kotlin
public data class PersonItem(
  public val `data`: Person, // Individual Person object within the list
  // ... Resource object values
)
```
```kotlin
public data class PersonList(
  public val `data`: List<PersonItem>, // List of PersonItem objects
  // ... Root values
)
```

To access the serialization and deserialization features, you need to use the `TypeAdapter` interface. To get the type adapter for your
specific class, use the generated `TypeAdapterFactory`:

```kotlin
// Gets adapter for a single instance of Person
val adapter = TypeAdapterFactory().getAdapter(PersonModel::class)

// Gets adapter for a list of Person instances
val listAdapter = TypeAdapterFactory().getAdapter(PersonList::class)

adapter.convertToString(personModel) // Produces JSON API String from a PersonModel instance
adapter.convertFromString(inputJsonString) // Produces PersonModel instance from JSON API String

listAdapter.convertToString(personList) // Produces JSON API String from a PersonList
listAdapter.convertFromString(inputJsonString) // Produces PersonList from JSON API String
```

### Nullability

JsonApiX relies on the `included` JSON array from the JSON API specification when deserializing relationships. If the input JSON API string
is missing the data of a non-nullable relationship in the `included` array, deserialization attempt will throw
a `JsonApiXMissingArgumentException`. On the other hand, nullable relationships will be evaluated as `null`.

### Errors

According to the JSON API specification, each object has an optional error array. The following interface is used as a root-level wrapper
for JSON API responses. It contains a nullable `errors` list, that is used to parse the errors.

```kotlin
interface JsonApiX<out Model> {
    val data: ResourceObject<Model>?
    val included: List<ResourceObject<*>>?
    val errors: List<Error>?
    val links: Links?
    val meta: Meta?
}
```

A single error is modeled to wrap the most common arguments of an error. Developer needs to make sure to model the error responses to match
this model.

```kotlin
@Serializable
@SerialName("error")
class DefaultError(
    val code: String,
    val title: String,
    val detail: String,
    val status: String
)
```

When using Retrofit, in the event of a network error a `HttpException` will be thrown. To extract the `DefaultError` model from a response, you can
use the `HttpException.asJsonXHttpException()` extension. The extension will return a `JsonXHttpException`, containing the original `response` as well as `errors` list.

```kotlin
// Using DefaultError
try {
    val person = io { sampleApiService.fetchPerson() }
} catch (exception: HttpException) {
    val jsonXHttpException = exception.asJsonXHttpException()
    val errors = jsonXHttpException.errors // List<DefaultError>
    // handle errors
    ...
}
```

### Custom error

Developers can define their own custom error models to adapt to the specific requirements.

Let's take this custom person error model as an example. 
Every custom error model should extend the `Error` interface and have a `JsonApiXError` annotation set. 

```kotlin
@Serializable
@JsonApiXError(type = "person")
data class PersonalError(
    val desc: String
) : Error
```

In this example, the annotation processor will automatically make the error type of a `Person` class to be a `PersonError` and use it as a type when deserializing errors array. Developer needs to make sure that the `type` parameter value in `JsonApiXError` matches the one in the `JsonApiX` above the original model.

To extract a custom error model (ex. `PersonalError`), clients should use a generic `asJsonXHttpException` in a similar way as explained in the previous chapter for `DefaultError`.

```kotlin
// Using custom error
try {
    val person = io { sampleApiService.fetchPerson() }
} catch (exception: HttpException) {
    val jsonXHttpException = exception.asJsonXHttpException<PersonError>()
    val errors = jsonXHttpException.errors // List<PersonError>
    // handle errors
    ...
}
```

## JsonApiModel - Handling `links` and `meta` JSON API fields

JSON API specification includes resources that are not necessarily a part of the original models.
JsonApiX provides a way to retrieve the `links` and `meta` values from the JSON API input, without including those fields in your model.

The links and meta live in the generated classes, such as `PersonModel` and `PersonList`, so your model only extends `JsonApiModel`,
as every model must (see [Usage](#usage)):

```kotlin
@Serializable
@JsonApiX("person")
data class Person(
    val name: String?,
    val surname: String,
    val age: Int,
    @HasOne("dog")
    val myFavoriteDog: Dog? = null
) : JsonApiModel()
```

`JsonApiModel` is an abstract class that holds the resource's `id` and `type`, with getters and setters. Decoding sets both from the
JSON API input. Encoding writes `id()` as the resource's `id`, or `"0"` when it's `null`.

The generated `PersonModel` and `PersonList` classes come with default meta and links, but we've provided options for customization. This allows you to tailor these elements to fit your specific project needs, enhancing flexibility and control.

### Links

Links can be included in the root object, relationships objects and resource objects (`data` key). 
By default, they have the following implementation:

```kotlin
class DefaultLinks(
    val self: String? = null,
    val related: String? = null,
    val first: String? = null,
    val last: String? = null,
    val next: String? = null,
    val prev: String? = null
) : Links
```

And the can be retrieved in the following way:

```kotlin
// Get root level links
personModel.rootLinks

// Get relationships links
personModel.relationshipsLinks

// Get resource object links
personModel.resourceObjectLinks
```

##### Custom links

Developers can define their own custom link models to adapt to the specific requirements.

Let's take this custom person links model as an example. 
Every custom links model must extend the `Links` interface and have a `JsonApiXLinks` annotation. 

```kotlin
@Serializable
@JsonApiXLinks(type = "person", placementStrategy = LinksPlacementStrategy.ROOT)
data class PersonLinks(
    val bioLink: String,
    val socialLink: String
) : Links
```

`LinksPlacementStartegy` enum is used to determine which links from the whole JSON API object will be replaced by a custom model.
It currently supports `ROOT`, `RELATIONSHIPS` and `DATA` links.


### Meta

In JSON API specification, Meta is an optional, free-format key-value pair. It doesn't have a predefined default model.
For that reason, in order to use the meta feature, developer must define a custom meta model for each class, where he wants to use it. 

Let's take this `PersonMeta` model as an example.
Every custom meta model must extend the `Meta` interface and have a `JsonApiXMeta` annotation.

```kotlin
@Serializable
@JsonApiXMeta(type = "person")
data class PersonMeta(val owner: String) : Meta
```

In this example, the annotation processor will automatically make the meta type of a `PersonModel` class to be `PersonMeta`.
Developer needs to make sure that the `type` parameter value in `JsonApiXMeta` matches the one in the `JsonApiX` above the original model.
To retrieve a meta object, a generic variant of the `meta()` method is used.

```kotlin
// Gets meta object
person.rootMeta
person.relationshipsMeta
person.resourceObjectMeta

```

## Retrofit Support

To enable Retrofit support, you need to add our custom converter in your retrofit builder. It takes an instance of `TypeAdapterFactory` as a
parameter. No additional steps needed.

```kotlin
Retrofit.Builder()
    .addConverterFactory(JsonXConverterFactory(TypeAdapterFactory()))
    .baseUrl("https://www.example.com")
    .build()
```

### Models in a Kotlin Multiplatform module

`retrofit` is JVM-only, so JsonApiX generates no Retrofit code into a multiplatform module. Android code can still use
Retrofit with those models, in an Android module that depends on the multiplatform one, or in the multiplatform
module's `androidMain` or `jvmMain`:

- add `retrofit` to it. It also needs `core`, which comes from the multiplatform module when that declares `core` with
  `api`, as in [Kotlin Multiplatform setup](#kotlin-multiplatform-setup);
- add the converter with the generated `TypeAdapterFactory`, as shown above;
- read errors with the `asJsonXHttpException` from `retrofit`, and pass it the `format` that JsonApiX generated. The
  generated `asJsonXHttpException()` without arguments from [Errors](#errors) doesn't exist for these models.

```kotlin
import com.infinum.jsonapix.format
import com.infinum.jsonapix.retrofit.asJsonXHttpException

try {
    val person = io { sampleApiService.fetchPerson() }
} catch (exception: HttpException) {
    val errors = exception.asJsonXHttpException<PersonalError>(format).errors // List<PersonalError>?
    // handle errors
    ...
}
```

For the default error model, use `asJsonXHttpException<DefaultError>(format)`.

## List of libraries used in the project

This chapter lists all the libraries that make a bigger contribution to the project. It is essential for any future
contributor to get
familiar with them, before starting any work on the project. For that reason, links to the documentation are a part of
the list.

- Kotlinx
  serialization - [kotlinx.serialization/serialization-guide.md](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
- Kotlin poet - https://square.github.io/kotlinpoet/

Along with the libraries documentation, the developer should also know his way around the JSON API, so here is a link to
the [specification](https://jsonapi.org/).

## Contributing

We believe that the community can help us improve and build better a product.
Please refer to our [contributing guide](CONTRIBUTING.md) to learn about the types of contributions we accept and the
process for submitting them.

To ensure that our community remains respectful and professional, we defined
a [code of conduct](CODE_OF_CONDUCT.md) <!-- and [coding standards](<link>) --> that we expect all contributors to
follow.

We appreciate your interest and look forward to your contributions.

## License

```text
Copyright 2024 Infinum

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

Maintained and sponsored by [Infinum](https://infinum.com).

<div align="center">
    <a href='https://infinum.com'>
    <picture>
        <source srcset="https://assets.infinum.com/brand/logo/static/white.svg" media="(prefers-color-scheme: dark)">
        <img src="https://assets.infinum.com/brand/logo/static/default.svg">
    </picture>
    </a>
</div>

