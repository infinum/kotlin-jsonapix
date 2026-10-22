package com.infinum.jsonapix.`data`.models

import com.infinum.jsonapix.core.resources.Attributes
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "Attributes_person")
public data class Attributes_Person(
  @SerialName(value = "age")
  public val age: Int,
  @SerialName(value = "name")
  public val name: String? = null,
  @SerialName(value = "surname")
  public val surname: String,
) : Attributes {
  public companion object {
    public fun fromOriginalObject(originalObject: Person): Attributes_Person = Attributes_Person(age = originalObject.age, name = originalObject.name, surname = originalObject.surname)
  }
}
