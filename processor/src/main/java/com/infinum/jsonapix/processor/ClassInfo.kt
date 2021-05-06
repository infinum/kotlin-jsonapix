package com.infinum.jsonapix.processor

import com.squareup.kotlinpoet.ClassName

data class ClassInfo(
    val type: String,
    val jsonWrapperClassName: ClassName,
    val resourceObjectClassName: ClassName,
    val attributesWrapperClassName: ClassName?,
    val includedWrapperClassName: ClassName?
)
