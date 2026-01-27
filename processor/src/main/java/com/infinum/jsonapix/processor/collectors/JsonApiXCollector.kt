package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.LinksInfo
import com.infinum.jsonapix.processor.MetaInfo
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.models.HasManyHolder
import com.infinum.jsonapix.processor.models.HasOneHolder
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.metadata.jvm.KotlinClassMetadata

internal class JsonApiXCollector(
    private val roundEnvironment: RoundEnvironment,
    private val elementUtils: Elements,
    private val typeUtils: Types,
    private val linksInfoMap: Map<String, LinksInfo>,
    private val metaInfoMap: Map<String, MetaInfo>,
    private val customErrors: Map<String, ClassName>
) : Collector<JsonApiXHolder> {

    companion object {
        val SUPPORTED = setOf(
            JsonApiX::class.java.name,
            HasOne::class.java.name,
            HasMany::class.java.name
        )
    }

    // TODO check this chain - throw erorr if wrong kind
    override fun collect(): Set<JsonApiXHolder> {
        return roundEnvironment.getElementsAnnotatedWith(JsonApiX::class.java)
            ?.filterIsInstance<Element>()
            ?.filter { it.kind == ElementKind.CLASS }
            ?.mapNotNull { element -> collectFromElement(element) }
            ?.toSet()
            ?: emptySet()
    }

    private fun collectFromElement(element: Element): JsonApiXHolder? {
        val type = element.getAnnotationParameterValue<JsonApiX, String> { type }
        val isNullable = element.getAnnotationParameterValue<JsonApiX, Boolean> { isNullable }

        val metadata = element.getAnnotation(Metadata::class.java) ?: return null
        val kmClass = (KotlinClassMetadata.readStrict(metadata) as? KotlinClassMetadata.Class)?.kmClass
            ?: return null

        val typeSpec = kmClass.toTypeSpec(
            ElementsClassInspector.create(false, elementUtils, typeUtils)
        )

        val className = ClassName(
            elementUtils.getPackageOf(element).toString(),
            element.simpleName.toString()
        )

        val (primitiveProperties, oneRelationships, manyRelationships) = separateProperties(typeSpec)

        return JsonApiXHolder(
            className = className,
            type = type,
            isNullable = isNullable,
            primitiveProperties = primitiveProperties,
            oneRelationships = oneRelationships,
            manyRelationships = manyRelationships,
            metaInfo = metaInfoMap[type],
            linksInfo = linksInfoMap[type],
            customError = customErrors[type]
        )
    }

    private fun separateProperties(
        typeSpec: TypeSpec
    ): Triple<List<PropertySpec>, List<HasOneHolder>, List<HasManyHolder>> {
        val primitiveProperties = mutableListOf<PropertySpec>()
        val oneRelationships = mutableListOf<HasOneHolder>()
        val manyRelationships = mutableListOf<HasManyHolder>()

        typeSpec.propertySpecs.filter { !it.delegated }.forEach { propertySpec ->
            val hasOneAnnotation = propertySpec.annotations.firstOrNull {
                it.typeName == HasOne::class.asTypeName()
            }
            val hasManyAnnotation = propertySpec.annotations.firstOrNull {
                it.typeName == HasMany::class.asTypeName()
            }

            when {
                hasOneAnnotation != null -> {
                    oneRelationships.add(
                        HasOneHolder(propertySpec)
                    )
                }
                hasManyAnnotation != null -> {
                    manyRelationships.add(
                        HasManyHolder(propertySpec)
                    )
                }
                else -> {
                    primitiveProperties.add(propertySpec)
                }
            }
        }

        return Triple(primitiveProperties, oneRelationships, manyRelationships)
    }

}
