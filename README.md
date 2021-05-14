# **JsonX**

##### Introduction

JsonX is a Kotlin multiplatform library still under development. In short, the idea of the library is to use annotation processing and code generation to transform regular Kotlin classes into JSON API representations of the same classes, with the ability to serialize/deserialize to/from Strings.

The following chapters describe methods and technologies used, some of the main ideas of the library and list all the tasks that are yet unfinished. This document should be taken as a roadmap to complete the library. Along the way all major solutions should be shortly explained with some examples if possible so any future developers would jump in easier.

##### List of libraries used in the project

This chapter lists all the libraries that make a bigger contribution to the project. It is essential for any future developer to get familiar with them, before starting any work on the project. For that reason, links to the documentation are a part of the list.

Kotlinx serialization - [kotlinx.serialization/serialization-guide.md](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
Kolin poet - https://square.github.io/kotlinpoet/

Along with the libraries documentation, the developer should also know his way around the JSON API, so here is a link to the specification.
