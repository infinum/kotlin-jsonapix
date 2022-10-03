package com.infinum.jsonapix.core.shared

import com.infinum.jsonapix.core.common.JsonApiXMissingArgumentException

fun <T : Any> requireNotNull(value: T?, missingArgument: String): T {
    if (value == null) {
        throw JsonApiXMissingArgumentException(missingArgument)
    } else {
        return value
    }
}

inline fun <T, R> Iterable<T>?.mapSafe(transform: (T) -> R): List<R> {
    return this?.map(transform) ?: emptyList()
}

inline fun <T, R> Iterable<T>?.flatMapSafe(transform: (T) -> Iterable<R>): List<R> {
    return this?.flatMap(transform) ?: emptyList()
}