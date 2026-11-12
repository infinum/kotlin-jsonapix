package com.infinum.jsonapix.core.adapters

@Suppress("StringLiteralDuplication")
interface TypeAdapter<Model> {
    fun rootLinks(): String = "links"
    fun resourceObjectLinks(): String = "links"
    fun relationshipsLinks(): String = "links"
    fun errors(): String = "error"
    fun rootMeta(): String = "meta"
    fun resourceObjectMeta(): String = "meta"
    fun relationshipsMeta(): String = "meta"
    fun convertFromString(input: String): Model
    fun convertToString(input: Model): String
}
