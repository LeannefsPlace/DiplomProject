package com.ivan.auth_service.service

import com.ivan.auth_service.kafka.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.session-results}") private val projectResultsTopic: String,
    @Value("\${app.kafka.topics.session-actions}") private val projectActionsTopic: String
) {

    fun sendSessionResult(result: SessionResultEvent) {
        kafkaTemplate.send(projectResultsTopic, result.eventId, result)
    }

    fun sendSessionAction(action: SessionActionEvent) {
        kafkaTemplate.send(projectActionsTopic, action.eventId, action)
    }
}