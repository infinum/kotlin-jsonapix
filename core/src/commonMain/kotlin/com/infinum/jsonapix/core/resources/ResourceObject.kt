package com.infinum.jsonapix.core.resources

interface ResourceObject<out Model> {
    val id: String
    val type: String
    val attributes: Attributes?
    val relationships: Relationships?
    val links: Links?
    val meta: Meta?

    fun original(included: List<ResourceObject<Any>>?): Model

    fun relationshipsLinks(): Map<String, Links?>? = relationships?.links

    fun relationshipsMeta(): Map<String, Meta?>? = relationships?.meta
}
