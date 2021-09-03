package com.infinum.jsonapix.core.resources

interface AttributesModel<out Model> {
    fun toOriginalOrNull(): Model?
}
