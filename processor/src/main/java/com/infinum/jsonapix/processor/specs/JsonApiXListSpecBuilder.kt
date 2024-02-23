package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiXList
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.DefaultLinks
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
        linksInfo: LinksInfo?,
        customError: ClassName?
    ): FileSpec {
        val modelClassName = ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST))
        val itemClassName = ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST_ITEM))

        val generatedName = JsonApiConstants.Prefix.JSON_API_X_LIST.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = getBasePropertySpecs(
            metaClassName = metaInfo?.rootClassName ?: Meta::class.asClassName(),
            rootLinksClassName = linksInfo?.rootLinks,
            customError = customError
        ).toMutableList()

        val params = getBaseParamSpecs(
            metaClassName = metaInfo?.rootClassName ?: Meta::class.asClassName(),
            rootLinksClassName = linksInfo?.rootLinks,
            customError = customError
        ).toMutableList()

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
                        JsonApiXList::class.asClassName().parameterizedBy(className, modelClassName)
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
                            itemClassName,
                            modelClassName,
                            metaInfo,
                            linksInfo
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
        itemClassName: ClassName,
        modelClassName: ClassName,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
    ): PropertySpec {

        val getterFunSpec = FunSpec.builder("get()")
            .addStatement("val items = data.map {")
            .addStatement("val original = it.original(included)")
            .addStatement(
                "%T(\n%L,\n%L,\n%L%T },\n%L,\n%L%T } )",
                itemClassName,
                "original",
                "it.links",
                "it.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? ",
                linksInfo?.relationshipsLinks ?: DefaultLinks::class,
                "it.meta",
                "it.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? ",
                metaInfo?.relationshipsClassNAme ?: Meta::class
            )
            .addStatement("}")
            .addStatement(
                "return %T(%L, %L, %L, %L)",
                modelClassName,
                "items",
                "links",
                "errors",
                "meta",
            )
            .build()

        val propertySpec = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            modelClassName, KModifier.OVERRIDE
        )
            .getter(getterFunSpec)
            .addAnnotation(
                AnnotationSpec.builder(Transient::class.asClassName())
                    .build()
            )

        return propertySpec.build()
    }
}
