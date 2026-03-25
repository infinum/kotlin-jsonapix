package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.extensions.appendIf
import com.infinum.jsonapix.processor.specs.Specs
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
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
import kotlin.properties.Delegates
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal object JsonApiXSpecBuilder : BaseJsonApiXSpecBuilder() {

    private val serializableClassName = Serializable::class.asClassName()

    private var isNullable by Delegates.notNull<Boolean>()
    override fun build(
        className: ClassName,
        isNullable: Boolean,
        type: String,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
        customError: ClassName?,
    ): FileSpec {
        val modelClassName = ClassName.bestGuess(
            className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL),
        )

        this.isNullable = isNullable
        val generatedName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName),
        )

        val properties = getBasePropertySpecs(
            metaClassName = metaInfo?.rootClassName ?: Meta::class.asClassName(),
            rootLinksClassName = linksInfo?.rootLinks,
            customError = customError,
        ).toMutableList()

        val params = getBaseParamSpecs(
            metaClassName = metaInfo?.rootClassName ?: Meta::class.asClassName(),
            rootLinksClassName = linksInfo?.rootLinks,
            customError = customError,
        ).toMutableList()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                resourceObjectClassName.copy(nullable = isNullable),
            ).build(),
        )

        properties.add(dataProperty(resourceObjectClassName))

        return FileSpec.builder(className.packageName, generatedName)
            .addImport(
                JsonApiConstants.Packages.CORE_RESOURCES,
                JsonApiConstants.Imports.RESOURCE_IDENTIFIER,
            )
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        JsonApiX::class.asClassName().parameterizedBy(className, modelClassName),
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(Specs.getSerialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build(),
                    )
                    .addProperties(properties)
                    .addProperty(
                        originalProperty(
                            modelClassName,
                            metaInfo,
                            linksInfo,
                        ),
                    )
                    .build(),
            )
            .build()
    }

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.DATA,
        resourceObject.copy(nullable = isNullable),
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA),
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    @Suppress("StringShouldBeRawString")
    private fun originalProperty(
        modelClassName: ClassName,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
    ): PropertySpec {
        val getterFunSpec = FunSpec.builder("get()")
            .addStatement("val original = data?.original(included)".appendIf("!!") { isNullable.not() })
            .addStatement(
                "val model = %T(\n%L,\n%L,\n%L,\n%L%T },\n%L,\n%L,\n%L,\n%L%T } )",
                modelClassName,
                "original",
                "links",
                "data?.links",
                "data?.relationshipsLinks()?.filterValues{ it != null }?.mapValues{ it.value as? ",
                linksInfo?.relationshipsLinks ?: DefaultLinks::class,
                "errors",
                "meta",
                "data?.meta",
                "data?.relationshipsMeta()?.filterValues{ it != null }?.mapValues{ it.value as? ",
                metaInfo?.relationshipsClassNAme ?: Meta::class,
            )
            .addStatement("return model")
            .build()

        val propertySpec = PropertySpec.builder(
            JsonApiConstants.Members.ORIGINAL,
            modelClassName,
            KModifier.OVERRIDE,
        )
            .getter(getterFunSpec)
            .addAnnotation(
                AnnotationSpec.builder(Transient::class.asClassName())
                    .build(),
            )

        return propertySpec.build()
    }
}
