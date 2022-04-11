package com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.OneRelationshipMember
import com.infinum.jsonapix.core.resources.ResourceIdentifier
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

internal object OneRelationshipModelFunSpecBuilder {

    fun build(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        return FunSpec.builder(JsonApiConstants.Members.TO_ONE_RELATIONSHIP_MODEL)
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(typeVariableName.copy(reified = true))
            .receiver(typeVariableName)
            .returns(OneRelationshipMember::class)
            .addParameter("type", String::class)
            .addParameter(ParameterSpec.builder("id", String::class).defaultValue("%S", "").build())
            .addStatement(
                "return %T(data = %T(type, id))",
                OneRelationshipMember::class.asClassName(),
                ResourceIdentifier::class.asClassName()
            )
            .build()
    }
}
