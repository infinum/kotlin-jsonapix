package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.Holder

internal interface Validator<H : Holder> {

    fun validate(elements: Set<H>): Set<H>
}
