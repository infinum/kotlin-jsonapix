package com.infinum.jsonapix.core.common

import java.lang.IllegalArgumentException

class JsonApiXMissingArgumentException : IllegalArgumentException() {
    override val message: String = "Missing attribute"
}
