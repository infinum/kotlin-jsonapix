package com.infinum.jsonapix.processor.specs.jsonxextensions

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Errors
import com.infinum.jsonapix.processor.ClassInfo
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ListItemResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ManyRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.OneRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.OriginalDataResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.FormatPropertySpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.WrapperSerializerPropertySpecBuilder
import com.infinum.jsonapix.retrofit.JsonXHttpException
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import retrofit2.HttpException

@SuppressWarnings("SpreadOperator")
internal class JsonXExtensionsSpecBuilder {

    private val specsMap = hashMapOf<ClassName, ClassInfo>()
    private val customLinks = mutableListOf<ClassName>()
    private val customErrors = mutableMapOf<String, ClassName>()
    private val customMeta = mutableListOf<ClassName>()

    @SuppressWarnings("LongParameterList")
    fun add(
        type: String,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
        isNullable: Boolean,
        data: ClassName,
        wrapper: ClassName,
        wrapperList: ClassName,
        resourceObject: ClassName,
        attributesObject: ClassName?,
        relationshipsObject: ClassName?,
        includedStatement: CodeBlock?,
        includedListStatement: CodeBlock?,
    ) {
        specsMap[data] = ClassInfo(
            type = type,
            metaInfo = metaInfo,
            linksInfo = linksInfo,
            isNullable = isNullable,
            jsonWrapperClassName = wrapper,
            jsonWrapperListClassName = wrapperList,
            resourceObjectClassName = resourceObject,
            attributesWrapperClassName = attributesObject,
            relationshipsObjectClassName = relationshipsObject,
            includedStatement = includedStatement,
            includedListStatement = includedListStatement,
        )
    }

    fun addCustomLinks(links: List<ClassName>) {
        customLinks.clear()
        customLinks.addAll(links)
    }

    fun addCustomErrors(map: Map<String, ClassName>) {
        customErrors.clear()
        customErrors.putAll(map)
    }

    fun addCustomMetas(meta: List<ClassName>) {
        customMeta.clear()
        customMeta.addAll(meta)
    }

    private fun decodeJsonApiErrorFunSpec(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        typeVariableName.bounds

        return FunSpec.builder(JsonApiConstants.Members.DECODE_JSON_API_ERROR)
            .addParameter(
                ParameterSpec(
                    name = JsonApiConstants.Members.ERROR_BODY,
                    type = String::class.asTypeName(),
                ),
            )
            .returns(List::class.asClassName().parameterizedBy(typeVariableName))
            .addModifiers(KModifier.INLINE)
            .beginControlFlow("return try")
            .addTypeVariable(
                typeVariableName.copy(
                    reified = true,
                    bounds = listOf(com.infinum.jsonapix.core.resources.Error::class.asTypeName()),
                ),
            )
            .addStatement(
                "format.decodeFromString<%T<%L>>(%L).errors",
                Errors::class,
                JsonApiConstants.Members.GENERIC_TYPE_VARIABLE,
                JsonApiConstants.Members.ERROR_BODY,
            )
            .nextControlFlow("catch (e: %T)", IllegalArgumentException::class.asClassName())
            .addStatement("emptyList()")
            .endControlFlow()
            .build()
    }

    private fun asJsonXHttpExceptionFunSpec(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)
        typeVariableName.bounds

        return FunSpec.builder(JsonApiConstants.Members.AS_JSON_X_HTTP_EXCEPTION)
            .receiver(HttpException::class)
            .returns(JsonXHttpException::class.asClassName().parameterizedBy(typeVariableName))
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(
                typeVariableName.copy(
                    reified = true,
                    bounds = listOf(com.infinum.jsonapix.core.resources.Error::class.asTypeName()),
                ),
            )
            .addStatement("val response = response()")
            .addStatement("val body = response?.errorBody()?.charStream()?.readText()")
            .addStatement(
                "val errors = if (body != null) %L<Model>(body) else emptyList()",
                JsonApiConstants.Members.DECODE_JSON_API_ERROR,
            )
            .addStatement("return JsonXHttpException(response(), errors)")
            .build()
    }

    @Suppress("SwallowedException")
    private fun hasRetrofitModule(): Boolean {
        return try {
            Class.forName("com.infinum.jsonapix.retrofit.JsonXHttpException")
            true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressWarnings("SpreadOperator", "LongMethod")
    fun build(): FileSpec {
        val fileSpec = FileSpec.builder(
            JsonApiConstants.Packages.JSONX,
            JsonApiConstants.FileNames.JSON_X_EXTENSIONS,
        )
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class)
                .addMember("%S", JsonApiConstants.FileNames.JSON_X_EXTENSIONS)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build(),
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION,
            *JsonApiConstants.Imports.KOTLINX,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_DISCRIMINATORS,
            *JsonApiConstants.Imports.CORE_EXTENSIONS,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            *JsonApiConstants.Imports.KOTLINX_MODULES,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.JSONX,
            *JsonApiConstants.Imports.JSON_X,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_SHARED,
            JsonApiConstants.Imports.MAP_SAFE,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_SHARED,
            JsonApiConstants.Imports.FLAT_MAP_SAFE,
        )

        specsMap.entries.forEach {
            fileSpec.addFunction(
                OriginalDataResourceObjectFunSpecBuilder.build(
                    it.key,
                    it.value.resourceObjectClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName,
                    it.value.metaInfo?.resourceObjectClassName,
                    it.value.linksInfo?.resourceObjectLinks,
                ),
            )
            fileSpec.addFunction(
                ResourceObjectFunSpecBuilder.build(
                    it.key,
                    it.value.resourceObjectClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName,
                ),
            )
            fileSpec.addFunction(
                ListItemResourceObjectFunSpecBuilder.build(
                    it.key,
                    it.value.resourceObjectClassName,
                    it.value.attributesWrapperClassName,
                    it.value.relationshipsObjectClassName,
                ),
            )
            fileSpec.addFunction(
                WrapperFunSpecBuilder.build(
                    it.key,
                    it.value.jsonWrapperClassName,
                    it.value.includedStatement?.toString(),
                ),
            )
            fileSpec.addFunction(
                WrapperListFunSpecBuilder.build(
                    it.key,
                    it.value.jsonWrapperListClassName,
                    it.value.includedListStatement?.toString(),
                ),
            )

            fileSpec.addFunction(SerializeFunSpecBuilder.build(it.key, it.value.isNullable))
            fileSpec.addFunction(SerializeListFunSpecBuilder.build(it.key))
        }

        fileSpec.addProperty(
            WrapperSerializerPropertySpecBuilder.build(
                specsMap,
                customLinks,
                customErrors,
                customMeta,
            ),
        )
        fileSpec.addProperty(FormatPropertySpecBuilder.build())
        fileSpec.addFunction(ManyRelationshipModelFunSpecBuilder.build())
        fileSpec.addFunction(OneRelationshipModelFunSpecBuilder.build())

        fileSpec.addFunction(decodeJsonApiErrorFunSpec())

        if (hasRetrofitModule()) {
            fileSpec.addImport(
                JsonApiConstants.Packages.CORE_RESOURCES,
                JsonApiConstants.Imports.ERRORS,
            )
            fileSpec.addFunction(asJsonXHttpExceptionFunSpec())
        }

        fileSpec.addFunction(DeserializeFunSpecBuilder.build())
        fileSpec.addFunction(DeserializeListFunSpecBuilder.build())

        return fileSpec.build()
    }
}
