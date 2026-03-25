package com.infinum.jsonapix.processor.specs.specbuilders

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.DefaultError
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.specs.models.LinksInfo
import com.infinum.jsonapix.processor.specs.models.MetaInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal object JsonApiModelSpecBuilder : BaseJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_MODEL
    override fun getRootClassName(rootType: ClassName): ClassName = rootType
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
            JsonApiConstants.Members.RESOURCE_OBJECT_LINKS.asParam(
                className = linksInfo?.resourceObjectLinks ?: DefaultLinks::class.asClassName(),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.NULL,
            ),
            JsonApiConstants.Members.RELATIONSHIPS_LINKS.asParam(
                className = Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    linksInfo?.relationshipsLinks?.copy(nullable = true) ?: DefaultLinks::class.asClassName().copy(nullable = true),
                ),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.EMPTY_MAP,
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
            JsonApiConstants.Members.RESOURCE_OBJECT_META.asParam(
                className = metaInfo?.resourceObjectClassName ?: Meta::class.asClassName(),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.NULL,
            ),
            JsonApiConstants.Members.RELATIONSHIPS_META.asParam(
                className = Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    metaInfo?.relationshipsClassNAme?.copy(nullable = true) ?: Meta::class.asClassName().copy(nullable = true),
                ),
                isNullable = true,
                defaultValue = JsonApiConstants.Defaults.EMPTY_MAP,
            ),
        )
    }
}
