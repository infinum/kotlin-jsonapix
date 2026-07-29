package com.infinum.jsonapix.processor.specs.jsonxextensions

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ListItemResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.OriginalDataResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.ResourceObjectFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.SerializeListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperFunSpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.funspecbuilders.WrapperListFunSpecBuilder
import com.infinum.jsonapix.processor.specs.models.ClassInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

/**
 * Builds one FileSpec per model containing that model's extension functions:
 * - Original.toResourceObject(meta, links)
 * - Model.toResourceObject()
 * - Item.toResourceObject()
 * - Model.toJsonApiX()
 * - List.toJsonApiXList()
 * - Model.toJsonApiXString(...)
 * - List.toJsonApiXString(...)
 */
@SuppressWarnings("SpreadOperator")
internal object JsonXModelExtensionsSpecBuilder {

    fun build(originalClass: ClassName, classInfo: ClassInfo): FileSpec {
        val fileName = "${originalClass.simpleName}Extensions"

        val fileSpec = FileSpec.builder(
            JsonApiConstants.Packages.JSONX,
            fileName,
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

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_SHARED,
            JsonApiConstants.Imports.MAP_SAFE,
        )

        fileSpec.addImport(
            JsonApiConstants.Packages.CORE_SHARED,
            JsonApiConstants.Imports.FLAT_MAP_SAFE,
        )

        fileSpec.addFunction(
            OriginalDataResourceObjectFunSpecBuilder.build(
                originalClass,
                classInfo.resourceObjectClassName,
                classInfo.attributesWrapperClassName,
                classInfo.relationshipsObjectClassName,
                classInfo.metaInfo?.resourceObjectClassName,
                classInfo.linksInfo?.resourceObjectLinks,
            ),
        )
        fileSpec.addFunction(
            ResourceObjectFunSpecBuilder.build(
                originalClass,
                classInfo.resourceObjectClassName,
                classInfo.attributesWrapperClassName,
                classInfo.relationshipsObjectClassName,
            ),
        )
        fileSpec.addFunction(
            ListItemResourceObjectFunSpecBuilder.build(
                originalClass,
                classInfo.resourceObjectClassName,
                classInfo.attributesWrapperClassName,
                classInfo.relationshipsObjectClassName,
            ),
        )
        fileSpec.addFunction(
            WrapperFunSpecBuilder.build(
                originalClass,
                classInfo.jsonWrapperClassName,
                classInfo.includedStatement?.toString(),
            ),
        )
        fileSpec.addFunction(
            WrapperListFunSpecBuilder.build(
                originalClass,
                classInfo.jsonWrapperListClassName,
                classInfo.includedListStatement?.toString(),
            ),
        )
        fileSpec.addFunction(SerializeFunSpecBuilder.build(originalClass, classInfo.isNullable))
        fileSpec.addFunction(SerializeListFunSpecBuilder.build(originalClass))

        return fileSpec.build()
    }
}
