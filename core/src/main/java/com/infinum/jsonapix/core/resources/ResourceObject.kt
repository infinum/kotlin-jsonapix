package com.infinum.jsonapix.core.resources


interface ResourceObject<out Model> {
    val id: String
    val type: String
    val attributes: AttributesModel<Model>?
    val relationships: RelationshipsModel?
    val links: LinksModel?

     fun getOriginalOrNull(): Model? {
         return attributes?.toOriginalOrNull()
     }
}
