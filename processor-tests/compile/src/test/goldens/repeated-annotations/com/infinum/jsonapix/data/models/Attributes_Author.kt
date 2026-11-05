package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_author")
public data class Attributes_Author(
  @SerialName(value = "name")
  public val name: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Author): Attributes_Author = Attributes_Author(name = originalObject.name)
  }
}
