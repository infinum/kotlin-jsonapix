package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Person : TypeAdapter<PersonModel> {
  override fun convertToString(input: PersonModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): PersonModel {
    val data = input.decodeJsonApiXString<Person,PersonModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootMeta(): String = "com.infinum.jsonapix.data.models.PersonRootMeta"

  override fun resourceObjectMeta(): String = "com.infinum.jsonapix.data.models.PersonResourceMeta"

  override fun relationshipsMeta(): String = "com.infinum.jsonapix.data.models.PersonRelationshipMeta"

  override fun errors(): String = "com.infinum.jsonapix.data.models.PersonalError"
}
