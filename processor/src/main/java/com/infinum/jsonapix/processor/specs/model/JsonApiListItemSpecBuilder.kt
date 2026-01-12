package com.infinum.jsonapix.processor.specs.model

import com.infinum.jsonapix.core.common.JsonApiConstants
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.core.resources.Meta
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal object JsonApiListItemSpecBuilder : BaseJsonApiModelSpecBuilder() {
    override fun getClassSuffixName(): String = JsonApiConstants.Suffix.JSON_API_LIST_ITEM
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
