package com.infinum.jsonapix.ui.examples.person

import com.infinum.jsonapix.data.models.Person

data class PersonState(
    val bodyString: String,
    val person: Person
)

class PersonEvent
