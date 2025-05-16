package com.ivan.user_service.util

import org.springframework.kafka.support.serializer.JsonDeserializer

class SafeJsonDeserializer<T : Any> : JsonDeserializer<T>() {
    override fun deserialize(topic: String, data: ByteArray?): T? {
        if (data == null || data.isEmpty()) {
            return null
        }
        return try {
            super.deserialize(topic, data)
        } catch (e: Exception) {
            println("Failed to deserialize message from topic $topic: ${String(data)}, ${e.message}")
            null
        }
    }
}