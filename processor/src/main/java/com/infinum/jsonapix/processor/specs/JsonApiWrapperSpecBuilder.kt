package com.infinum.jsonapix.processor.specs

import com.infinum.jsonapix.core.JsonApModel
import com.infinum.jsonapix.core.resources.ResourceObject
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal object JsonApiWrapperSpecBuilder {

    private const val SERIAL_NAME_PLACEHOLDER = "value = %S"
    private const val GENERATED_CLASS_PREFIX = "JsonApiSerializable_"
    private const val KEY_DATA = "data"
    private const val KEY_ERRORS = "errors"
    private const val INCLUDED_KEY = "included"
    private val serializableClassName = Serializable::class.asClassName()
    private val transientClassName = Transient::class.asClassName()

    fun build(
        pack: String,
        className: String,
        type: String,
        attributes: List<String>,
        oneRelationships: Map<String, TypeName>,
        manyRelationships: Map<String, TypeName>
    ): FileSpec {
        val dataClass = ClassName(pack, className)
        val generatedName = "$GENERATED_CLASS_PREFIX$className"
        val resourceObjectClassName = ClassName(pack, "ResourceObject_$className")

        val properties = mutableListOf<PropertySpec>()
        val params = mutableListOf<ParameterSpec>()

        params.add(
            ParameterSpec.builder(
                KEY_DATA,
                resourceObjectClassName
            ).build()
        )

        properties.add(dataProperty(resourceObjectClassName))

        params.add(
            nullParam(
                INCLUDED_KEY,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )
        properties.add(
            nullProperty(
                INCLUDED_KEY,
                List::class.asClassName().parameterizedBy(
                    ResourceObject::class.asClassName()
                        .parameterizedBy(getAnnotatedAnyType())
                ).copy(nullable = true)
            )
        )

        params.add(
            ParameterSpec.builder(
                KEY_ERRORS,
                List::class.parameterizedBy(String::class)
                    .copy(nullable = true)
            ).defaultValue("%L", "null")
                .build()
        )

        properties.add(errorsProperty())

        return FileSpec.builder(pack, generatedName)
            .addImport("com.infinum.jsonapix.core.resources", "ResourceIdentifier")
            .addType(
                TypeSpec.classBuilder(generatedName)
                    .addSuperinterface(
                        JsonApModel::class.asClassName().parameterizedBy(dataClass)
                    )
                    .addAnnotation(serializableClassName)
                    .addAnnotation(serialNameSpec(type))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(params)
                            .build()
                    )
                    .addProperties(properties)
                    .addFunction(getOriginalFunSpec(dataClass, attributes, oneRelationships, manyRelationships))
                    .build()
            )
            .build()
    }

    private fun getAnnotatedAnyType(): TypeName {
        val contextual = AnnotationSpec.builder(Contextual::class).build()
        return ANY.copy(annotations = ANY.annotations + contextual)
    }

    private fun nullProperty(
        name: String,
        typeName: TypeName,
        isTransient: Boolean = false
    ): PropertySpec = PropertySpec.builder(
        name,
        typeName,
        KModifier.OVERRIDE
    ).apply {
        if (isTransient) {
            addAnnotation(transientClassName)
        } else {
            addAnnotation(serialNameSpec(name))
        }
        initializer(name)
    }.build()


    private fun nullParam(name: String, typeName: TypeName): ParameterSpec =
        ParameterSpec.builder(name, typeName, KModifier.OVERRIDE)
            .defaultValue("%L", null)
            .build()

    private fun serialNameSpec(name: String) =
        AnnotationSpec.builder(SerialName::class).addMember(SERIAL_NAME_PLACEHOLDER, name)
            .build()

    private fun dataProperty(resourceObject: ClassName): PropertySpec = PropertySpec.builder(
        KEY_DATA, resourceObject
    ).addAnnotation(
        serialNameSpec(KEY_DATA)
    )
        .initializer(KEY_DATA).addModifiers(KModifier.OVERRIDE)
        .build()

    private fun errorsProperty(): PropertySpec = PropertySpec.builder(
        KEY_ERRORS,
        List::class.parameterizedBy(String::class).copy(nullable = true),
        KModifier.OVERRIDE
    )
        .addAnnotation(serialNameSpec(KEY_ERRORS))
        .initializer(KEY_ERRORS)
        .build()

    private fun getOriginalFunSpec(className: ClassName, attributes: List<String>, oneRelationships: Map<String, TypeName>, manyRelationships: Map<String, TypeName>): FunSpec {
        var codeString = "return ${className.simpleName}("
        val builder = FunSpec.builder("getOriginal")
            .returns(className)
            .addModifiers(KModifier.OVERRIDE)
        attributes.forEach {
            codeString += "$it = data.attributes?.$it!!, "
        }
        val typeParams = mutableListOf<TypeName>()
        oneRelationships.forEach {
            codeString += "${it.key} = included?.firstOrNull { it.type == data.relationships?.${it.key}?.data?.type && it.id == data.relationships.${it.key}.data.id }?.getOriginalOrNull() as %T, "
            typeParams.add(it.value)
        }
        manyRelationships.forEach {
            codeString += "${it.key} = included?.filter { data.relationships?.${it.key}?.data?.contains(ResourceIdentifier(it.type, it.id)) == true }?.map { it.getOriginalOrNull() } as %T, "
            typeParams.add(it.value)
        }
        codeString += ")"
        return builder.addStatement(codeString, *typeParams.toTypedArray()).build()
    }
}
