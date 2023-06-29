package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.JsonApiXModel
import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Links
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
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

internal abstract class BasicJsonApiModelSpecBuilder {

    abstract fun getClassSuffixName(): String
    abstract fun getRootClassName(rootType: ClassName): TypeName
    fun build(
        className: ClassName,
        isRootNullable: Boolean,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?
    ): FileSpec {
        val generatedName = className.simpleName.withName(getClassSuffixName())


        val params = getParams(className, isRootNullable, metaInfo, linksInfo)
        val props = params.map { it.toPropSpec() }

        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build()
                    )
                    .addSuperinterface(
                        JsonApiXModel::class.asClassName().parameterizedBy(getRootClassName(className).copy(nullable = isRootNullable))
                    )
                    .addProperties(props)
                    .build()
            )
            .build()
    }

    private fun getParams(
        className: ClassName,
        isRootNullable: Boolean,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?
    ): List<ParameterSpec> {
        return listOf(
            JsonApiConstants.Keys.DATA.asParam(getRootClassName(className), isRootNullable),
            JsonApiConstants.Keys.TYPE.asParam(String::class.asClassName(), true),
            JsonApiConstants.Keys.ID.asParam(String::class.asClassName(), true),

            JsonApiConstants.Members.ROOT_LINKS.asParam(Links::class.asClassName(), true),
            JsonApiConstants.Members.RESOURCE_OBJECT_LINKS.asParam(Links::class.asClassName(), true),
            JsonApiConstants.Members.RELATIONSHIPS_LINKS.asParam(
                Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    Links::class.asClassName().copy(nullable = true),
                ),
                true,
            ),

            JsonApiConstants.Keys.ERRORS.asParam(List::class.asClassName().parameterizedBy(Error::class.asClassName()), true),

            JsonApiConstants.Members.ROOT_META.asParam(metaInfo?.rootClassName ?: Meta::class.asClassName(), true),
            JsonApiConstants.Members.RESOURCE_OBJECT_META.asParam(metaInfo?.resourceObjectClassName ?: Meta::class.asClassName(), true),
            JsonApiConstants.Members.RELATIONSHIPS_META.asParam(
                Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    metaInfo?.relationshipsClassNAme?.copy(nullable = true) ?: Meta::class.asClassName().copy(nullable = true),
                ),
                true
            ),
        )
    }

    private fun String.asParam(className: TypeName, isNullable: Boolean): ParameterSpec {
        return ParameterSpec.builder(
            this,
            className.copy(isNullable)
        )
            .build()
    }

    private fun ParameterSpec.toPropSpec(): PropertySpec {
        return PropertySpec.builder(
            name,
            type,
        )
            .initializer(name).addModifiers(KModifier.OVERRIDE)
            .build()
    }

}