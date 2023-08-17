package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
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

internal object JsonApiXListSpecBuilder : BaseJsonApiXSpecBuilder() {

    private val serializableClassName = Serializable::class.asClassName()

    override fun build(
        className: ClassName,
        isNullable: Boolean,
        type: String,
        metaInfo: MetaInfo?,
    ): FileSpec {
        val generatedName = JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = getBasePropertySpecs(metaInfo?.rootClassName ?: Meta::class.asClassName()).toMutableList()
        val params = getBaseParamSpecs(metaInfo?.rootClassName ?: Meta::class.asClassName()).toMutableList()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                List::class.asClassName().parameterizedBy(resourceObjectClassName)
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
                        JsonApiXList::class.asClassName().parameterizedBy(className)
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
        List::class.asClassName().parameterizedBy(resourceObject)
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA)
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun originalProperty(
        className: ClassName
    ): PropertySpec {
        val codeString = "${JsonApiConstants.Keys.DATA}.map { it.${JsonApiConstants.Members.ORIGINAL}(included) }"
        val builder = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            List::class.asClassName().parameterizedBy(className), KModifier.OVERRIDE
        ).addAnnotation(AnnotationSpec.builder(Transient::class.asClassName()).build())

        return builder.initializer(codeString).build()
    }
}
