package com.infinum.jsonapix.core.common

import java.lang.IllegalArgumentException

class JsonApiXMissingArgumentException(
    missingArgument: String,
) : IllegalArgumentException() {
    override val message: String = "Missing attribute = $missingArgument"
}
