package com.infinum.jsonapix.processor

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.CHAR_SEQUENCE
import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MAP_ENTRY
import com.squareup.kotlinpoet.MUTABLE_COLLECTION
import com.squareup.kotlinpoet.MUTABLE_ITERABLE
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MUTABLE_MAP_ENTRY
import com.squareup.kotlinpoet.MUTABLE_SET
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.U_BYTE
import com.squareup.kotlinpoet.U_BYTE_ARRAY
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.U_INT_ARRAY
import com.squareup.kotlinpoet.U_LONG
import com.squareup.kotlinpoet.U_LONG_ARRAY
import com.squareup.kotlinpoet.U_SHORT
import com.squareup.kotlinpoet.U_SHORT_ARRAY
import com.squareup.kotlinpoet.asTypeName

internal class PropertyTypesSeparator(private val classType: TypeSpec) {

    private val primitiveFields = mutableListOf<PropertySpec>()
    private val compositeFields = mutableListOf<PropertySpec>()

    init {
        processClassParameters()
    }

    fun getPrimitiveProperties(): List<PropertySpec> {
        return primitiveFields.toList()
    }

    fun getCompositeProperties(): List<PropertySpec> {
        return compositeFields.toList()
    }

    fun getManyRelationships(): List<PropertySpec> {
        return compositeFields.filter { it.isManyRelationship() }
    }

    fun getOneRelationships(): List<PropertySpec> {
        return compositeFields.filter { it.isOneRelationship() }
    }

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

    private fun TypeName.isPrimitiveOrString(): Boolean {
        return when (this) {
            STRING,
            BOOLEAN,
            BYTE,
            SHORT,
            INT,
            LONG,
            CHAR,
            FLOAT,
            DOUBLE,
            CHAR_SEQUENCE,
            NUMBER,
            BOOLEAN_ARRAY,
            BYTE_ARRAY,
            SHORT_ARRAY,
            INT_ARRAY,
            LONG_ARRAY,
            CHAR_ARRAY,
            FLOAT_ARRAY,
            DOUBLE_ARRAY,
            U_BYTE,
            U_SHORT,
            U_INT,
            U_LONG,
            U_BYTE_ARRAY,
            U_SHORT_ARRAY,
            U_INT_ARRAY,
            U_LONG_ARRAY,
            STRING.copy(nullable = true),
            BOOLEAN.copy(nullable = true),
            BYTE.copy(nullable = true),
            SHORT.copy(nullable = true),
            INT.copy(nullable = true),
            LONG.copy(nullable = true),
            CHAR.copy(nullable = true),
            FLOAT.copy(nullable = true),
            DOUBLE.copy(nullable = true),
            CHAR_SEQUENCE.copy(nullable = true),
            NUMBER.copy(nullable = true),
            BOOLEAN_ARRAY.copy(nullable = true),
            BYTE_ARRAY.copy(nullable = true),
            SHORT_ARRAY.copy(nullable = true),
            INT_ARRAY.copy(nullable = true),
            LONG_ARRAY.copy(nullable = true),
            CHAR_ARRAY.copy(nullable = true),
            FLOAT_ARRAY.copy(nullable = true),
            DOUBLE_ARRAY.copy(nullable = true),
            U_BYTE.copy(nullable = true),
            U_SHORT.copy(nullable = true),
            U_INT.copy(nullable = true),
            U_LONG.copy(nullable = true),
            U_BYTE_ARRAY.copy(nullable = true),
            U_SHORT_ARRAY.copy(nullable = true),
            U_INT_ARRAY.copy(nullable = true),
            U_LONG_ARRAY.copy(nullable = true) -> true
            else -> false
        }
    }

    private fun ParameterizedTypeName.isCollection(): Boolean {

        return this.typeArguments.all { it.isPrimitiveOrString() } && when (rawType) {
            ARRAY,
            ITERABLE,
            COLLECTION,
            LIST,
            SET,
            MAP,
            MAP_ENTRY,
            MUTABLE_ITERABLE,
            MUTABLE_COLLECTION,
            MUTABLE_LIST,
            MUTABLE_SET,
            MUTABLE_MAP,
            MUTABLE_MAP_ENTRY,
            ARRAY.copy(nullable = true),
            ITERABLE.copy(nullable = true),
            COLLECTION.copy(nullable = true),
            LIST.copy(nullable = true),
            SET.copy(nullable = true),
            MAP.copy(nullable = true),
            MAP_ENTRY.copy(nullable = true),
            MUTABLE_ITERABLE.copy(nullable = true),
            MUTABLE_COLLECTION.copy(nullable = true),
            MUTABLE_LIST.copy(nullable = true),
            MUTABLE_SET.copy(nullable = true),
            MUTABLE_MAP.copy(nullable = true),
            MUTABLE_MAP_ENTRY.copy(nullable = true) -> true
            else -> false
        }
    }
}
