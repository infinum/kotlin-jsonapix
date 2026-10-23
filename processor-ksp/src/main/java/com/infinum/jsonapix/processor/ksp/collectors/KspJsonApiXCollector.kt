package com.infinum.jsonapix.processor.ksp.collectors

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.infinum.jsonapix.annotations.HasMany
import com.infinum.jsonapix.annotations.HasOne
import com.infinum.jsonapix.annotations.JsonApiX
import com.infinum.jsonapix.processor.collectors.Collector
import com.infinum.jsonapix.processor.ksp.extensions.getArgumentValue
import com.infinum.jsonapix.processor.ksp.extensions.isOfType
import com.infinum.jsonapix.processor.ksp.extensions.modelClassName
import com.infinum.jsonapix.processor.models.AttributeProperty
import com.infinum.jsonapix.processor.models.JsonApiXHolder
import com.infinum.jsonapix.processor.models.RelationshipProperty
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import kotlinx.serialization.SerialName

internal class KspJsonApiXCollector(
    private val models: List<KSClassDeclaration>,
) : Collector<JsonApiXHolder> {

    override fun collect(): Set<JsonApiXHolder> = models.mapTo(linkedSetOf(), ::collectFromDeclaration)

    private fun collectFromDeclaration(declaration: KSClassDeclaration): JsonApiXHolder {
        val annotation = declaration.annotations.first { it.isOfType(JsonApiX::class) }
        val (attributes, oneRelationships, manyRelationships) = separateProperties(declaration)

        return JsonApiXHolder(
            className = declaration.modelClassName(),
            type = annotation.getArgumentValue<String>(JsonApiX::type.name),
            isNullable = annotation.getArgumentValue<Boolean>(JsonApiX::isNullable.name),
            attributes = attributes,
            oneRelationships = oneRelationships,
            manyRelationships = manyRelationships,
        )
    }

    private fun separateProperties(
        declaration: KSClassDeclaration,
    ): Triple<List<AttributeProperty>, List<RelationshipProperty>, List<RelationshipProperty>> {
        val attributes = mutableListOf<AttributeProperty>()
        val oneRelationships = mutableListOf<RelationshipProperty>()
        val manyRelationships = mutableListOf<RelationshipProperty>()

        val typeParameterResolver = declaration.typeParameters.toTypeParameterResolver()
        val constructorParameterNames = declaration.primaryConstructor?.parameters.orEmpty()
            .mapNotNullTo(mutableSetOf()) { it.name?.asString() }

        declaration.getDeclaredProperties().filter { !it.isDelegated() }.forEach { propertyDeclaration ->
            val annotations = propertyDeclaration.annotations.toList()

            val hasOneAnnotation = annotations.firstOrNull { it.isOfType(HasOne::class) }
            val hasManyAnnotation = annotations.firstOrNull { it.isOfType(HasMany::class) }
            val serialName = annotations.firstOrNull { it.isOfType(SerialName::class) }
                ?.getArgumentValue<String>(SerialName::value.name)

            val property = propertyDeclaration.toPropertySpec(
                isConstructorProperty = propertyDeclaration.simpleName.asString() in constructorParameterNames,
                typeParameterResolver = typeParameterResolver,
            )

            when {
                hasOneAnnotation != null -> oneRelationships.add(
                    RelationshipProperty(
                        property = property,
                        type = hasOneAnnotation.getArgumentValue(HasOne::type.name),
                        serialName = serialName,
                    ),
                )

                hasManyAnnotation != null -> manyRelationships.add(
                    RelationshipProperty(
                        property = property,
                        type = hasManyAnnotation.getArgumentValue(HasMany::type.name),
                        serialName = serialName,
                    ),
                )

                else -> attributes.add(AttributeProperty(property = property, serialName = serialName))
            }
        }

        return Triple(attributes, oneRelationships, manyRelationships)
    }

    /**
     * Builds the property the way kotlinpoet-metadata does for the KAPT collector, as far as generators
     * use it. The attributes class declares each attribute property again, and a constructor property
     * only stays in the constructor if its initializer is its own name. It carries no annotations: the
     * ones read above are holder fields instead, and any others aren't carried over.
     */
    private fun KSPropertyDeclaration.toPropertySpec(
        isConstructorProperty: Boolean,
        typeParameterResolver: TypeParameterResolver,
    ): PropertySpec {
        val name = simpleName.asString()
        return PropertySpec.builder(name, type.toTypeName(typeParameterResolver))
            .mutable(isMutable)
            .apply {
                // kotlinpoet-metadata leaves out the public modifier, which is the default.
                getVisibility().toKModifier()?.takeIf { it != KModifier.PUBLIC }?.let { addModifiers(it) }
                if (isConstructorProperty) initializer(name)
            }
            .build()
    }
}
