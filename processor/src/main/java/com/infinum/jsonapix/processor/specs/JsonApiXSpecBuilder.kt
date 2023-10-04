package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApiX
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Meta
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
import kotlin.properties.Delegates

internal object JsonApiXSpecBuilder : BaseJsonApiXSpecBuilder() {

    private val serializableClassName = Serializable::class.asClassName()

    private var isNullable by Delegates.notNull<Boolean>()
    override fun build(
        className: ClassName,
        isNullable: Boolean,
        type: String,
        metaInfo: MetaInfo?,
    ): FileSpec {
        val modelClassName = ClassName.bestGuess(className.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_MODEL))

        this.isNullable = isNullable
        val generatedName = JsonApiConstants.Prefix.JSON_API_X.withName(className.simpleName)
        val resourceObjectClassName = ClassName(
            className.packageName,
            JsonApiConstants.Prefix.RESOURCE_OBJECT.withName(className.simpleName)
        )

        val properties = getBasePropertySpecs(metaInfo?.rootClassName ?: Meta::class.asClassName()).toMutableList()
        val params = getBaseParamSpecs(metaInfo?.rootClassName ?: Meta::class.asClassName()).toMutableList()

        params.add(
            ParameterSpec.builder(
                JsonApiConstants.Keys.DATA,
                resourceObjectClassName.copy(nullable = isNullable)
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
                        JsonApiX::class.asClassName().parameterizedBy(className, modelClassName)
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
                            modelClassName,
                            metaInfo,
                            isNullable,
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        JsonApiConstants.Keys.DATA,
        resourceObject.copy(nullable = isNullable)
    ).addAnnotation(
        Specs.getSerialNameSpec(JsonApiConstants.Keys.DATA)
    )
        .initializer(JsonApiConstants.Keys.DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun originalProperty(
        modelClassName: ClassName,
        metaInfo: MetaInfo?,
        isNullable: Boolean,
    ): PropertySpec {

        val getterFunSpec = FunSpec.builder("get()")
            .addStatement(if (isNullable) "val original = data?.original(included)" else "val original = data.original(included)")
            .addStatement(
                "val model = %T(%L,%L,%L,%L,%L,%L,%L,%L?.filterValues{ it != null }?.mapValues{ it.value as? %T } )",
                modelClassName,
                "original",
                "links",
                if (isNullable) "data?.links" else "data.links",
                (if (isNullable) "data?.relationshipsLinks()" else "data.relationshipsLinks()") + "\n?.filterValues{ it != null }",
                "errors",
                "meta",
                if (isNullable) "data?.meta" else "data.meta",
                if (isNullable) "data?.relationshipsMeta()" else "data.relationshipsMeta()",
                metaInfo?.relationshipsClassNAme ?: Meta::class
            )
            .addStatement("return model")
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
