package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@SuppressWarnings("SpreadOperator")
internal object JsonApiXSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    @SuppressWarnings("LongMethod")
    fun build(
        className: ClassName,
        type: String
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = mutableListOf<PropertySpec>()
        val params = mutableListOf<ParameterSpec>()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                resourceObjectClassName
            ).build()
        )

        properties.add(dataProperty(resourceObjectClassName))

        params.add(
            Specs.getNullParamSpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )
        properties.add(
            Specs.getNullPropertySpec(
                JsonApiConstants.Keys.INCLUDED,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.ERRORS,
                List::class.parameterizedBy(String::class)
                    .copy(nullable = true)
            ).defaultValue("%L", "null")
                .build()
        )

        properties.add(errorsProperty())

        return FileSpec.builder(className.packageName, generatedName)
            .addImport(
                JsonApiConstants.Packages.CORE_RESOURCES,
                JsonApiConstants.Imports.RESOURCE_IDENTIFIER
            )
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        JsonApiX::class.asClassName().parameterizedBy(className)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(Specs.getSerialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build()
                    )
                    .addProperties(properties)
                    .addProperty(
                        originalProperty(
                            className
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun getAnnotatedAnyType(): TypeName {
        val contextual = AnnotationSpec.builder(Contextual::class).build()
        return ANY.copy(annotations = ANY.annotations + contextual)
    }

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.DATA, resourceObject
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA)
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.ERRORS,
        List::class.parameterizedBy(String::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(Specs.getSerialNameSpec(JsonApiConstants.Keys.ERRORS))
        .initializer(JsonApiConstants.Keys.ERRORS)
        .build()

    private fun originalProperty(
        className: ClassName
    ): PropertySpec {
        val codeString = "data.original(included!!)"
        val builder = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            className, KModifier.OVERRIDE
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(codeString).build()
    }
}
