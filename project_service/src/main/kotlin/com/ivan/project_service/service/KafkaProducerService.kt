package com.ivan.project_service.service

import com.ivan.project_service.kafka.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.project-results}") private val projectResultsTopic: String,
    @Value("\${app.kafka.topics.project-actions}") private val projectActionsTopic: String,
) {

    fun sendProjectResult(result: ProjectResultEvent) {
        kafkaTemplate.send(projectResultsTopic, result.eventId, result)
    }

    fun sendProjectAction(action: ProjectActionEvent) {
        kafkaTemplate.send(projectActionsTopic, action.eventId, action)
    }
}