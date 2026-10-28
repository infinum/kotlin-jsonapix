package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.adapters.TypeAdapter
import com.infinum.jsonapix.decodeJsonApiXString
import com.infinum.jsonapix.toJsonApiXString
import kotlin.String

public class TypeAdapter_Company : TypeAdapter<CompanyModel> {
  override fun convertToString(input: CompanyModel): String = input.toJsonApiXString(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())

  override fun convertFromString(input: String): CompanyModel {
    val data = input.decodeJsonApiXString<Company,CompanyModel>(rootLinks(), resourceObjectLinks(), relationshipsLinks(), rootMeta(), resourceObjectMeta(), relationshipsMeta(), errors())
    return data.original
  }
}
