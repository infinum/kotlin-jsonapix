package com.infinum.jsonapix.core.resources

interface Relationships {
    val links: Map<String, Links?>
    val meta: Map<String, Meta?>
}
