package com.ivan.gateway_service.service.query

import com.ivan.gateway_service.kafka.BackupCommandEvent
import com.ivan.gateway_service.kafka.BackupResultEvent
import com.ivan.gateway_service.kafka.SkillCommandEvent
import com.ivan.gateway_service.kafka.SkillResultEvent
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
class BackupQueryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.backup-commands}") private val requestTopic: String
) {
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Any>>()

    suspend fun getSkillEvent(
        request: BackupCommandEvent,
        timeout: Duration = Duration.ofSeconds(5),
    ): BackupResultEvent {
        val future = CompletableFuture<Any>()

        pendingRequests[request.eventId] = future

        kafkaTemplate.send(
            requestTopic, request.eventId, request
        ).await()

        return try {
            withTimeout(timeout.toMillis()) {
                future.get() as BackupResultEvent
            }
        } finally {
            pendingRequests.remove(request.eventId)
        }
    }

    @KafkaListener(topics = ["\${app.kafka.topics.backup-results}"],
        containerFactory = "backupResultsListenerContainerFactory")
    fun handleResponse(
        response: BackupResultEvent,
        ack: Acknowledgment
    ) {
        if (pendingRequests.containsKey(response.eventId)) {
            pendingRequests[response.eventId]?.complete(response)
        }
        ack.acknowledge()
    }
}