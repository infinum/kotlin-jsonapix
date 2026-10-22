package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_tag")
public data class Attributes_Tag(
  @SerialName(value = "label")
  public val label: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Tag): Attributes_Tag = Attributes_Tag(label = originalObject.label)
  }
}
