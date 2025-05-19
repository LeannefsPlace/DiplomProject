package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.*
import com.ivan.gateway_service.util.auth_util.SessionStorage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class KafkaListenerService(private val sessionStorage: SessionStorage) {
    @KafkaListener(topics = ["\${app.kafka.topics.project-actions}"],
        containerFactory = "projectActionsListenerContainerFactory",)
    fun handleProjectAction(
        response: ProjectActionEvent,
        ack: Acknowledgment
    ) {
        if (response.actionType != ProjectActionType.UPDATE) {
            response.userId?.let { sessionStorage.updateSessionByUserId(it)}
        }
        ack.acknowledge()
    }

    @KafkaListener(topics = ["\${app.kafka.topics.session-actions}"],
        containerFactory = "sessionActionsListenerContainerFactory",)
    fun handleSessionAction(
        response: SessionActionEvent,
        ack: Acknowledgment
    ) {
        if (response.sessionCommandType != SessionCommandType.VERIFY) {
            response.userId?.let { sessionStorage.updateTokensByUserId(it)}
        }
        ack.acknowledge()
    }

    @KafkaListener(topics = ["\${app.kafka.topics.user-actions}"],
        containerFactory = "userActionsListenerContainerFactory",)
    fun handleUserAction(
        response: UserActionEvent,
        ack: Acknowledgment
    ) {
        if (response.actionType != "USER_UPDATED") {
            response.userId?.let { sessionStorage.updateTokensByUserId(it)}
        }
        ack.acknowledge()
    }
}