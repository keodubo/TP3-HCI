package com.comprartir.mobile.core.network.serialization

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes [Instant] values using backend date format "yyyy-MM-dd HH:mm:ss"
 */
object BackendDateSerializer : KSerializer<Instant> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "BackendDateString",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): Instant {
        val dateString = decoder.decodeString()
        val localDateTime = LocalDateTime.parse(dateString, formatter)
        return localDateTime.toInstant(ZoneOffset.UTC)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        val localDateTime = LocalDateTime.ofInstant(value, ZoneOffset.UTC)
        encoder.encodeString(localDateTime.format(formatter))
    }
}
