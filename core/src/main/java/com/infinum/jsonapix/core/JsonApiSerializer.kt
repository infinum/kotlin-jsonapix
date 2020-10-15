package com.infinum.jsonapix.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

class JsonApiSerializer<T : JsonApiSerializable>(private val dataKSerializer: KSerializer<T>) :
    KSerializer<T> {

    override val descriptor: SerialDescriptor = //DummySerializable.serializer(dataKSerializer).descriptor
        buildClassSerialDescriptor(
        "DummySerializable",
            String.serializer().descriptor,
            dataKSerializer.descriptor
    ) {
        element<String>("type")
        element("data", dataKSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): T {
        var type: String = ""
        var data: T? = null
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> {
                        type = decodeStringElement(descriptor, 0)
                    }
                    1 -> {
                        data = decodeSerializableElement(dataKSerializer.descriptor, 1, dataKSerializer)
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> break
                }
            }
        }
        return data!!
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.dataType)
            encodeSerializableElement(descriptor, 1, dataKSerializer, value)
        }
    }
}