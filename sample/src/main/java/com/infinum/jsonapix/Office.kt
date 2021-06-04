package com.infinum.jsonapix

import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import kotlinx.serialization.Serializable

@Serializable
@JsonApiX("office")
class Office(
    @HasOne("person")
    val manager: Person
    )