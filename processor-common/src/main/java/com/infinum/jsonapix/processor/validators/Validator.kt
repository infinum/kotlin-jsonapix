package com.infinum.jsonapix.processor.validators

import com.infinum.jsonapix.processor.models.Holder

public interface Validator<H : Holder> {

    public fun validate(elements: Set<H>): Set<H>
}
