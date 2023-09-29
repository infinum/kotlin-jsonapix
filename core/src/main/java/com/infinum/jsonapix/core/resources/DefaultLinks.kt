package com.infinum.jsonapix.core.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("links")
class DefaultLinks(
    @SerialName("self") val self: String? = null,
    @SerialName("related") val related: String? = null,
    @SerialName("first") val first: String? = null,
    @SerialName("last") val last: String? = null,
    @SerialName("next") val next: String? = null,
    @SerialName("prev") val prev: String? = null
) : Links
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultLinks

        if (self != other.self) return false
        if (related != other.related) return false
        if (first != other.first) return false
        if (last != other.last) return false
        if (next != other.next) return false
        return prev == other.prev
    }

    override fun toString(): String {
        return "DefaultLinks(self=$self, related=$related, first=$first, last=$last, next=$next, prev=$prev)"
    }


}
