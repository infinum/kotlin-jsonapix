package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.processor.models.Holder

public interface Collector<H : Holder> {

    public fun collect(): Set<H>
}
