package com.infinum.jsonapix.core.common

import java.lang.IllegalArgumentException

// todo NIKOLA add attribute logging
class JsonApiXMissingArgumentException : IllegalArgumentException() {
    override val message: String = "Missing attribute"
}
