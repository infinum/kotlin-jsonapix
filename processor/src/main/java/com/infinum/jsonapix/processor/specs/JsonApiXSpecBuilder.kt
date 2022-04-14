package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.Prefix.withName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal object JsonApiXSpecBuilder : BaseJsonApiXSpecBuilder() {

    private val serializableClassName = Serializable::class.asClassName()

    override fun build(
        className: ClassName,
        type: String,
        metaClassName: ClassName?
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = getBasePropertySpecs(metaClassName).toMutableList()
        val params = getBaseParamSpecs(metaClassName).toMutableList()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                resourceObjectClassName
            ).build()
        )

        properties.add(dataProperty(resourceObjectClassName))

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

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.DATA,
        resourceObject
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA)
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun originalProperty(
        className: ClassName
    ): PropertySpec {
        val codeString = "${JsonApiConstants.Keys.DATA}.${JsonApiConstants.Members.ORIGINAL}(included)"
        val builder = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            className, KModifier.OVERRIDE
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(codeString).build()
    }
}
