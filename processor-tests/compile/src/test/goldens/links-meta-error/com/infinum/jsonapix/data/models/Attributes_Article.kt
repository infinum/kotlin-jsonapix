package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_article")
public data class Attributes_Article(
  @SerialName(value = "title")
  public val title: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Article): Attributes_Article = Attributes_Article(title = originalObject.title)
  }
}
