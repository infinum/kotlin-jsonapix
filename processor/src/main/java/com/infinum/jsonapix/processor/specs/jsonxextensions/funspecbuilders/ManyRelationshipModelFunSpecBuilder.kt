package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.ManyRelationshipMember
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

internal object ManyRelationshipModelFunSpecBuilder {

    fun build(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        return FunSpec.builder(JsonApiConstants.Members.TO_MANY_RELATIONSHIP_MODEL)
            .receiver(Collection::class.asClassName().parameterizedBy(typeVariableName))
            .returns(ManyRelationshipMember::class)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .addParameter("type", String::class)
            .addParameter(
                ParameterSpec.builder(
                    "idMapper",
                    Function1::class.asClassName()
                        .parameterizedBy(typeVariableName, String::class.asClassName())
                ).defaultValue("{ \"\" }").build()
            )
            .addStatement(
                "return %T(data = map { %T(type, idMapper(it)) })",
                ManyRelationshipMember::class.asClassName(),
                ResourceIdentifier::class.asClassName()
            )
            .build()
    }
}
