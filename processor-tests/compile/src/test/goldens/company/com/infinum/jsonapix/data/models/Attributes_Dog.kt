package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_dog")
public data class Attributes_Dog(
  @SerialName(value = "age")
  public val age: Int,
  @SerialName(value = "name")
  public val name: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Dog): Attributes_Dog = Attributes_Dog(age = originalObject.age, name = originalObject.name)
  }
}
