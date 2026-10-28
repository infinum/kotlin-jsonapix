package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXListString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapterList_Person : TypeAdapter<PersonList> {
  override fun convertToString(input: PersonList): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): PersonList {
    val data = input.decodeJsonApiXListString<Person,PersonList>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }

  override fun rootMeta(): String = "com.infinum.jsonapix.data.models.PersonRootMeta"

  override fun resourceObjectMeta(): String = "com.infinum.jsonapix.data.models.PersonResourceMeta"

  override fun relationshipsMeta(): String = "com.infinum.jsonapix.data.models.PersonRelationshipMeta"

  override fun errors(): String = "com.infinum.jsonapix.data.models.PersonalError"
}
