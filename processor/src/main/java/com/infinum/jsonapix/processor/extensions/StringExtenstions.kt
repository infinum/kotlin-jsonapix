package com.infinum.jsonapix.processor.extensions

public fun String.appendIf(
    appendString: String,
    predicate: (String) -> Boolean,
): String =
    if (predicate(this)) {
        this + appendString
    } else {
        this
    }
