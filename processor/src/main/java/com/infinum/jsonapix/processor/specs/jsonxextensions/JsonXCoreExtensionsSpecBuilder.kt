package com.infinum.jsonapix.processor.specs.jsonxextensions

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.Error
import com.infinum.jsonapix.core.resources.Errors
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.DeserializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ManyRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.OneRelationshipModelFunSpecBuilder
import com.infinum.jsonapix.retrofit.JsonXHttpException
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import retrofit2.HttpException

/**
 * Builds JsonXCoreExtensions.kt containing shared utility functions:
 * - toManyRelationshipModel()
 * - toOneRelationshipModel()
 * - decodeJsonApiError()
 * - asJsonXHttpException() (conditional on retrofit module)
 * - decodeJsonApiXString()
 * - decodeJsonApiXListString()
 */
internal object JsonXCoreExtensionsSpecBuilder {

    @SuppressWarnings("SpreadOperator")
    fun build(): FileSpec {
        val fileSpec = FileSpec.builder(
            JsonApiConstants.Packages.JSONX,
            JsonApiConstants.FileNames.JSON_X_CORE_EXTENSIONS,
        )

        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmName::class)
                .addMember("%S", JsonApiConstants.FileNames.JSON_X_EXTENSIONS)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE).build(),
        )
        fileSpec.addAnnotation(
            AnnotationSpec.builder(JvmMultifileClass::class)
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
            JsonApiConstants.Packages.JSONX,
            *JsonApiConstants.Imports.JSON_X,
        )

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

    private fun decodeJsonApiErrorFunSpec(): FunSpec {
        val typeVariableName =
            TypeVariableName.invoke(JsonApiConstants.Members.GENERIC_TYPE_VARIABLE)

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
                    bounds = listOf(Error::class.asTypeName()),
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

        return FunSpec.builder(JsonApiConstants.Members.AS_JSON_X_HTTP_EXCEPTION)
            .receiver(HttpException::class)
            .returns(JsonXHttpException::class.asClassName().parameterizedBy(typeVariableName))
            .addModifiers(KModifier.INLINE)
            .addTypeVariable(
                typeVariableName.copy(
                    reified = true,
                    bounds = listOf(Error::class.asTypeName()),
                ),
            )
            .addStatement("val response = response()")
            .addStatement("val body = response?.errorBody()?.charStream()?.readText()")
            .addStatement(
                "val errors = if (body != null) %L<Model>(body) else emptyList()",
                JsonApiConstants.Members.DECODE_JSON_API_ERROR,
            )
            .addStatement("return JsonXHttpException(response, errors)")
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
}
