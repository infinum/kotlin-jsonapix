package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.processor.models.Holder

internal interface Collector<H : Holder> {

    fun collect(): Set<H>
}
