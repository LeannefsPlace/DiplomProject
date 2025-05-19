package com.ivan.gateway_service.service.query

import com.ivan.gateway_service.kafka.ProjectTaskCommandEvent
import com.ivan.gateway_service.kafka.ProjectTaskResultEvent
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class ProjectTasksQueryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.project-task-commands}") private val requestTopic: String
) {
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Any>>()

    suspend fun getProjectTaskEvent(
        request: ProjectTaskCommandEvent,
        timeout: Duration = Duration.ofSeconds(5),
    ): ProjectTaskResultEvent {
        val future = CompletableFuture<Any>()
        println("ProjectTaskEvent: ${request.eventId} ${request.commandType}")
        pendingRequests[request.eventId] = future

        kafkaTemplate.send(
            requestTopic, request.eventId, request
        ).await()

        return try {
            withTimeout(timeout.toMillis()) {
                future.get() as ProjectTaskResultEvent
            }
        } finally {
            pendingRequests.remove(request.eventId)
        }
    }

    @KafkaListener(topics = ["\${app.kafka.topics.project-task-results}"],
        containerFactory = "projectTaskResultsListenerContainerFactory")
    fun handleResponse(
        response: ProjectTaskResultEvent,
        ack: Acknowledgment
    ) {
        if (pendingRequests.containsKey(response.eventId)) {
            pendingRequests[response.eventId]?.complete(response)
        }
        ack.acknowledge()
    }
}