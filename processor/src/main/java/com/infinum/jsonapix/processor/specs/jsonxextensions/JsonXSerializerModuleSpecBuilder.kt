package com.infinum.jsonapix.processor.specs.jsonxextensions

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.FormatPropertySpecBuilder
import com.infinum.jsonapix.processor.specs.jsonxextensions.propertyspecbuilders.WrapperSerializerPropertySpecBuilder
import com.infinum.jsonapix.processor.specs.models.ClassInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

/**
 * Builds JsonXSerializerModule.kt containing:
 * - jsonApiXSerializerModule (polymorphic serializer registrations)
 * - format (Json instance configured with the serializer module)
 */
internal object JsonXSerializerModuleSpecBuilder {

    fun build(
        specsMap: HashMap<ClassName, ClassInfo>,
        customLinks: List<ClassName>,
        customErrors: Map<String, ClassName>,
        customMeta: List<ClassName>,
    ): FileSpec {
        val fileSpec = FileSpec.builder(
            JsonApiConstants.Packages.JSONX,
            JsonApiConstants.FileNames.JSON_X_SERIALIZER_MODULE,
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
            JsonApiConstants.Packages.KOTLINX_SERIALIZATION_MODULES,
            *JsonApiConstants.Imports.KOTLINX_MODULES,
        )

        fileSpec.addProperty(
            WrapperSerializerPropertySpecBuilder.build(
                specsMap,
                customLinks,
                customErrors,
                customMeta,
            ),
        )
        fileSpec.addProperty(FormatPropertySpecBuilder.build())

        return fileSpec.build()
    }
}
