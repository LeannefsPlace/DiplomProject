package com.ivan.project_task_service.service

import com.ivan.project_task_service.kafka.ProjectTaskActionEvent
import com.ivan.project_task_service.kafka.ProjectTaskResultEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.project-task-results}") private val projectResultsTopic: String,
    @Value("\${app.kafka.topics.project-task-actions}") private val projectActionsTopic: String,
) {

    fun sendProjectTaskResult(result: ProjectTaskResultEvent) {
        kafkaTemplate.send(projectResultsTopic, result.eventId, result)
    }

    fun sendProjectTaskAction(action: ProjectTaskActionEvent) {
        kafkaTemplate.send(projectActionsTopic, action.eventId, action)
    }
}