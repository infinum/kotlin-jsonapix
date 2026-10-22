package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_address")
public data class Attributes_Address(
  @SerialName(value = "street")
  public val street: String,
  @SerialName(value = "number")
  public val number: Int,
  @SerialName(value = "country")
  public val country: String,
  @SerialName(value = "city")
  public val city: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Address): Attributes_Address = Attributes_Address(street = originalObject.street, number = originalObject.number, country = originalObject.country, city = originalObject.city)
  }
}
