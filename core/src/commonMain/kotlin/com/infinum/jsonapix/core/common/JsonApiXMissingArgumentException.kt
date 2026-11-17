package com.infinum.jsonapix.core.common

class JsonApiXMissingArgumentException(missingArgument: String) : IllegalArgumentException() {
    override val message: String = "Missing attribute = $missingArgument"
}
