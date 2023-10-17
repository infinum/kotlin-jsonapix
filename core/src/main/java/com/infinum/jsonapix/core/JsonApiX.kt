package com.infinum.jsonapix.core

import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.core.resources.ResourceObject


/**
 * The `JsonApiX` interface represents a generic JSON API response.
 * It is designed to encapsulate the main components of a JSON API response,
 * including the primary data, included resources, errors, links, and meta information.
 *
 * @param Data The type of the primary data object, typically a domain model.
 * @param Model The type of the original response model, which may include additional fields not covered by this interface.
 */
interface JsonApiX<out Data, out Model> {
    val data: ResourceObject<Data>?
    val included: List<ResourceObject<*>>?
    val errors: List<Error>?
    val links: Links?
    val meta: Meta?

    val original: Model
}
