package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

internal class PropertyTypesSeparator(
    private val classType: TypeSpec,
) {
    private val primitiveFields = mutableListOf<PropertySpec>()
    private val compositeFields = mutableListOf<PropertySpec>()

    init {
        processClassParameters()
    }

    fun getPrimitiveProperties(): List<PropertySpec> = primitiveFields.toList()

    fun getCompositeProperties(): List<PropertySpec> = compositeFields.toList()

    fun getManyRelationships(): List<PropertySpec> = compositeFields.filter { it.isManyRelationship() }

    fun getOneRelationships(): List<PropertySpec> = compositeFields.filter { it.isOneRelationship() }

    private fun processClassParameters() {
        classType.propertySpecs.filter { !it.delegated }.forEach { property ->
            if (property.isRelationship()) {
                compositeFields.add(property)
            } else {
                primitiveFields.add(property)
            }
        }
    }

    private fun PropertySpec.isRelationship(): Boolean =
        annotations.any {
            it.typeName == HasOne::class.asTypeName() || it.typeName == HasMany::class.asTypeName()
        }

    private fun PropertySpec.isManyRelationship(): Boolean =
        annotations.any {
            it.typeName == HasMany::class.asTypeName()
        }

    private fun PropertySpec.isOneRelationship(): Boolean =
        annotations.any {
            it.typeName == HasOne::class.asTypeName()
        }
}
