package com.infinum.jsonapix.core.resources


interface ResourceObject<out Model> {
    val id: String
    val type: String
    val attributes: Attributes<Model>?
    val relationships: Relationships?
    val links: Links?

     fun originalOrNull(): Model? {
         return attributes?.originalOrNull()
     }
}
