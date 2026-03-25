package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.common.JsonApiConstants.withName
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

internal object JsonApiListSpecBuilder : BaseJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_LIST

    override fun getRootClassName(rootType: ClassName): TypeName {
        val itemType = ClassName.bestGuess(rootType.canonicalName.withName(JsonApiConstants.Suffix.JSON_API_LIST_ITEM))
        return List::class.asClassName().parameterizedBy(itemType)
    }

    override fun getParams(
        className: ClassName,
        isRootNullable: Boolean,
        metaInfo: MetaInfo?,
        linksInfo: LinksInfo?,
        customError: ClassName?,
    ): List<ParameterSpec> {
        return listOf(
            JsonApiConstants.Keys.DATA.asParam(
                className = getRootClassName(className),
                isNullable = isRootNullable,
                defaultValue = JsonApiConstants.Defaults.NULL.takeIf { isRootNullable },
            ),
            JsonApiConstants.Members.ROOT_LINKS.asParam(
                className = linksInfo?.rootLinks ?: DefaultLinks::class.asClassName(),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.NULL,
            ),
            JsonApiConstants.Keys.ERRORS.asParam(
                className = List::class.asClassName().parameterizedBy(customError ?: DefaultError::class.asClassName()),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.NULL,
            ),
            JsonApiConstants.Members.ROOT_META.asParam(
                className = metaInfo?.rootClassName ?: Meta::class.asClassName(),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.NULL,
            ),
        )
    }
}
