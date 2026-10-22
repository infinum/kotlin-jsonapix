package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_comment")
public data class Attributes_Comment(
  @SerialName(value = "body")
  public val body: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Comment): Attributes_Comment = Attributes_Comment(body = originalObject.body)
  }
}
