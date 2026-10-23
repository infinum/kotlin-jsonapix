package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_owner")
public data class Attributes_Owner(
  @SerialName(value = "first_name")
  public val firstName: String,
  @SerialName(value = "last_name")
  public val lastName: String? = null,
  @SerialName(value = "age")
  public val age: Int,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Owner): Attributes_Owner = Attributes_Owner(firstName = originalObject.firstName, lastName = originalObject.lastName, age = originalObject.age)
  }
}
