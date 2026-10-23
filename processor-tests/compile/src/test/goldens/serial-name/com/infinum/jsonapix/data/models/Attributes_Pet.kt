package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_pet")
public data class Attributes_Pet(
  @SerialName(value = "name")
  public val name: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Pet): Attributes_Pet = Attributes_Pet(name = originalObject.name)
  }
}
