package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Serializable

internal abstract class BaseJsonApiModelSpecBuilder {

    private val serializableClassName = Serializable::class.asClassName()

    abstract fun getClassSuffixName(): String
    abstract fun getRootClassName(rootType: ClassName): TypeName
    abstract fun getParams(
        className: ClassName,
        isRootNullable: Boolean,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
        customError: ClassName?
    ): List<ParameterSpec>

    fun build(
        className: ClassName,
        isRootNullable: Boolean,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
        customError: ClassName?
    ): FileSpec {
        val generatedName = className.simpleName.withName(getClassSuffixName())

        val params = getParams(className, isRootNullable, metaInfo, linksInfo, customError)
        val props = params.map { it.toPropSpec() }

        return FileSpec.builder(className.packageName, generatedName)
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(serializableClassName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build()
                    )
                    .addProperties(props)
                    .build()
            )
            .build()
    }

    protected fun String.asParam(className: TypeName, isNullable: Boolean, defaultValue: String? = null): ParameterSpec {
        return ParameterSpec.builder(
            this,
            className.copy(isNullable),
        ).apply {
            if (defaultValue != null) {
                defaultValue(defaultValue)
            }
        }.build()
    }

    private fun ParameterSpec.toPropSpec(): PropertySpec {
        return PropertySpec.builder(
            name,
            type,
        )
            .initializer(name)
            .build()
    }
}
