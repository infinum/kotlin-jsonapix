package com.infinum.jsonapix.processor.collectors

import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.extensions.getAnnotationParameterValue
import com.infinum.jsonapix.processor.extensions.getStringParameterValue
import com.infinum.jsonapix.processor.models.AttributeProperty
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.RelationshipProperty
import com.squareup.kotlinpoet.ClassName
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
import kotlinx.serialization.SerialName

internal class JsonApiXCollector(
    private val roundEnvironment: RoundEnvironment,
    private val elementUtils: Elements,
    private val typeUtils: Types,
) : Collector<JsonApiXHolder> {

    override fun collect(): Set<JsonApiXHolder> {
        return roundEnvironment.getElementsAnnotatedWith(JsonApiX::class.java)
            ?.filterIsInstance<Element>()
            ?.filter { it.kind == ElementKind.CLASS }
            ?.mapNotNull { element -> collectFromElement(element) }
            ?.toSet()
            ?: emptySet()
    }

    @Suppress("ReturnCount")
    private fun collectFromElement(element: Element): JsonApiXHolder? {
        val type = element.getAnnotationParameterValue<JsonApiX, String> { type }
        val isNullable = element.getAnnotationParameterValue<JsonApiX, Boolean> { isNullable }

        val metadata = element.getAnnotation(Metadata::class.java) ?: return null
        val kmClass = (KotlinClassMetadata.readStrict(metadata) as? KotlinClassMetadata.Class)?.kmClass
            ?: return null

        val typeSpec = kmClass.toTypeSpec(
            ElementsClassInspector.create(false, elementUtils, typeUtils),
        )

        val className = ClassName(
            elementUtils.getPackageOf(element).toString(),
            element.simpleName.toString(),
        )

        val (attributes, oneRelationships, manyRelationships) = separateProperties(typeSpec)

        return JsonApiXHolder(
            className = className,
            type = type,
            isNullable = isNullable,
            attributes = attributes,
            oneRelationships = oneRelationships,
            manyRelationships = manyRelationships,
        )
    }

    private fun separateProperties(
        typeSpec: TypeSpec,
    ): Triple<List<AttributeProperty>, List<RelationshipProperty>, List<RelationshipProperty>> {
        val attributes = mutableListOf<AttributeProperty>()
        val oneRelationships = mutableListOf<RelationshipProperty>()
        val manyRelationships = mutableListOf<RelationshipProperty>()

        typeSpec.propertySpecs.filter { !it.delegated }.forEach { propertySpec ->
            val hasOneAnnotation = propertySpec.annotations.firstOrNull {
                it.typeName == HasOne::class.asTypeName()
            }
            val hasManyAnnotation = propertySpec.annotations.firstOrNull {
                it.typeName == HasMany::class.asTypeName()
            }
            val serialName = propertySpec.annotations.firstOrNull {
                it.typeName == SerialName::class.asTypeName()
            }?.getStringParameterValue(SerialName::value.name)

            // Holders carry the @SerialName value as a field rather than as an annotation on the property.
            val property = propertySpec.toBuilder()
                .apply { annotations.removeAll { it.typeName == SerialName::class.asTypeName() } }
                .build()

            when {
                hasOneAnnotation != null -> oneRelationships.add(
                    RelationshipProperty(
                        property = property,
                        type = hasOneAnnotation.getStringParameterValue(HasOne::type.name),
                        serialName = serialName,
                    ),
                )

                hasManyAnnotation != null -> manyRelationships.add(
                    RelationshipProperty(
                        property = property,
                        type = hasManyAnnotation.getStringParameterValue(HasMany::type.name),
                        serialName = serialName,
                    ),
                )

                else -> attributes.add(AttributeProperty(property = property, serialName = serialName))
            }
        }

        return Triple(attributes, oneRelationships, manyRelationships)
    }

    companion object {
        val SUPPORTED = setOf(
            JsonApiX::class.java.name,
            HasOne::class.java.name,
            HasMany::class.java.name,
        )
    }
}
