package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_book")
public data class Attributes_Book(
  @SerialName(value = "title")
  public val title: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Book): Attributes_Book = Attributes_Book(title = originalObject.title)
  }
}
