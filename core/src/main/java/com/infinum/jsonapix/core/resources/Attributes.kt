package com.infinum.jsonapix.core.resources

interface Attributes<out Model> {
    fun getOriginalOrNull(): Model?
}
