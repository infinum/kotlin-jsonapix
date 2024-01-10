package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Attributes
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal object AttributesSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()
    private val serialNameTypeName = SerialName::class.asTypeName()

    fun build(
        className: ClassName,
        attributes: List<PropertySpec>,
        type: String
    ): TypeSpec {
        val generatedName = JsonApiConstants.Prefix.ATTRIBUTES.withName(className.simpleName)
        val parameterSpecs = attributes.map {
            ParameterSpec.builder(it.name, it.type)
                .apply {
                    if (it.annotations.missingTypeName(serialNameTypeName)) {
                        addAnnotation(Specs.getSerialNameSpec(it.name))
                    }
                    if (it.type.isNullable) {
                        defaultValue("%L", null)
                    }
                }
                .build()
        }

        return TypeSpec.classBuilder(generatedName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(Attributes::class.asClassName())
            .addAnnotation(serializableClassName)
            .addAnnotation(
                Specs.getSerialNameSpec(JsonApiConstants.Prefix.ATTRIBUTES.withName(type))
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(parameterSpecs)
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(fromOriginalObjectSpec(className, generatedName, attributes))
                    .build()
            )
            .addProperties(attributes)
            .build()
    }

    private fun List<AnnotationSpec>.missingTypeName(typeName: TypeName): Boolean {
        return indexOfFirst { it.typeName == typeName } < 0
    }

    private fun fromOriginalObjectSpec(
        originalClass: ClassName,
        generatedName: String,
        attributes: List<PropertySpec>
    ): FunSpec {
        val constructorString = attributes.joinToString(", ") {
            "${it.name} = originalObject.${it.name}"
        }
        return FunSpec.builder(JsonApiConstants.Members.FROM_ORIGINAL_OBJECT)
            .addParameter(
                ParameterSpec.builder("originalObject", originalClass).build()
            )
            .addStatement("return %L($constructorString)", generatedName)
            .returns(ClassName.bestGuess(generatedName))
            .build()
    }
}
