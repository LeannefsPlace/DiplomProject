package com.ivan.gateway_service.service.query

import com.ivan.gateway_service.kafka.SkillCommandEvent
import com.ivan.gateway_service.kafka.SkillResultEvent
import com.ivan.gateway_service.kafka.UserCommandEvent
import com.ivan.gateway_service.kafka.UserResultEvent
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
class UserQueryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.user-commands}") private val requestTopic: String
) {
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Any>>()

    suspend fun getUserEvent(
        request: UserCommandEvent,
        timeout: Duration = Duration.ofSeconds(5),
    ): UserResultEvent {
        val future = CompletableFuture<Any>()

        pendingRequests[request.eventId] = future

        kafkaTemplate.send(
            requestTopic, request.eventId, request
        ).await()

        return try {
            withTimeout(timeout.toMillis()) {
                future.get() as UserResultEvent
            }
        } finally {
            pendingRequests.remove(request.eventId)
        }
    }

    @KafkaListener(topics = ["\${app.kafka.topics.user-results}"],
        containerFactory = "userResultsListenerContainerFactory")
    fun handleResponse(
        response: UserResultEvent,
        ack: Acknowledgment
    ) {
        if (pendingRequests.containsKey(response.eventId)) {
            pendingRequests[response.eventId]?.complete(response)
        }
        ack.acknowledge()
    }
}