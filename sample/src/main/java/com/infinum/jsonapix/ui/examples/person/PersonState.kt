package com.infinum.jsonapix.ui.examples.person

import com.infinum.jsonapix.data.models.Person

data class PersonState(
    val bodyString: String,
    val person: Person,
    val rootLink: String?,
    val resourceObjectLink: String?,
    val relationshipsLink: String?,
    val meta: String?
)

class PersonEvent
