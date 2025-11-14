package com.comprartir.mobile.core.network.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serializer that accepts numbers or strings and always decodes them as [String].
 * Backend identifiers are often numeric, but the app models use strings to avoid
 * refactoring the whole database layer. This keeps both worlds compatible.
 */
object FlexibleStringSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "FlexibleString",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): String {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            (element as? JsonPrimitive)?.content ?: element.toString()
        } else {
            decoder.decodeString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
