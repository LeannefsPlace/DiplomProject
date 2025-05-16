package com.ivan.gateway_service.service.query

import com.ivan.gateway_service.kafka.SessionCommandEvent
import com.ivan.gateway_service.kafka.SessionResultEvent
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
class AuthQueryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.session-commands}") private val requestTopic: String
) {
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Any>>()

    suspend fun getAuthEvent(
        request: SessionCommandEvent,
        timeout: Duration = Duration.ofSeconds(5),
    ): SessionResultEvent{
        val future = CompletableFuture<Any>()

        println("sessionCommandEvent: ${request.sessionCommandType}")

        pendingRequests[request.eventId] = future

        kafkaTemplate.send(
            requestTopic, request.eventId, request
        ).await()

        return try {
            withTimeout(timeout.toMillis()) {
                future.get() as SessionResultEvent
            }
        } finally {
            pendingRequests.remove(request.eventId)
        }
    }

    @KafkaListener(topics = ["\${app.kafka.topics.session-results}"],
        containerFactory = "sessionResultsListenerContainerFactory")
    fun handleResponse(
        response: SessionResultEvent,
        ack: Acknowledgment
    ) {
        if (pendingRequests.containsKey(response.eventId)) {
            pendingRequests[response.eventId]?.complete(response)
        }
        ack.acknowledge()
    }
}