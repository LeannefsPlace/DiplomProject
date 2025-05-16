package com.ivan.auth_service.service

import com.ivan.auth_service.kafka.*
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class KafkaMessagingService(
    private val sessionService: SessionService,
    private val kafkaProducerService: KafkaProducerService
) {

    @KafkaListener(
        topics = ["\${app.kafka.topics.session-commands}"],
        containerFactory = "sessionCommandsListenerContainerFactory"
    )
    suspend fun dispatchMessage(command: SessionCommandEvent, ack: Acknowledgment) {
        when (command.sessionCommandType) {
            SessionCommandType.EXPIRE -> expire(command)
            SessionCommandType.EXPIRE_ALL -> expireAll(command)
            SessionCommandType.EXPIRE_ALL_EXCEPT_CURRENT -> expireAllExceptCurrent(command)
            SessionCommandType.VERIFY -> verify(command)
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.user-actions}"],
        containerFactory = "userActionsListenerContainerFactory"
    )
    suspend fun reactToMessage(command: UserActionEvent, ack: Acknowledgment) {
        if (command.actionType == "ROLE_UPDATED" || command.actionType == "PASSWORD_UPDATED")
            if(sessionService.expireAll(requireNotNull(command.userId))) {
                kafkaProducerService.sendSessionAction(
                    SessionActionEvent(
                        eventId = command.eventId,
                        userId = command.userId,
                        sessionCommandType = SessionCommandType.EXPIRE_ALL
                    )
                )
            }
    }

    suspend fun expire(command: SessionCommandEvent) {
        try{
            if(sessionService.expireSession(requireNotNull(command.sessionId))) {
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = true
                    )
                )

                kafkaProducerService.sendSessionAction(
                    SessionActionEvent(
                        eventId = command.eventId,
                        userId = command.userId,
                        sessionCommandType = SessionCommandType.EXPIRE
                    )
                )
            }
            else
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = false,
                        errorMessage = "something went wrong"
                    )
                )
        }
        catch (e: Exception){
            kafkaProducerService.sendSessionResult(
                SessionResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "something went wrong: ${e.message}"
                )
            )
        }
    }
    suspend fun expireAll(command: SessionCommandEvent){
        try{
            if(sessionService.expireAll(requireNotNull(command.userId))) {
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = true
                    )
                )
                kafkaProducerService.sendSessionAction(
                    SessionActionEvent(
                        eventId = command.eventId,
                        userId = command.userId,
                        sessionCommandType = SessionCommandType.EXPIRE_ALL
                    )
                )
            }
            else
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = false,
                        errorMessage = "something went wrong"
                    )
                )
        }
        catch (e: Exception){
            kafkaProducerService.sendSessionResult(
                SessionResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "something went wrong: ${e.message}"
                )
            )
        }
    }

    suspend fun expireAllExceptCurrent(command: SessionCommandEvent){
        try{
            if(sessionService.expireAllExceptCurrent(requireNotNull(command.userId),requireNotNull(command.sessionId))) {
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = true
                    )
                )

                kafkaProducerService.sendSessionAction(
                    SessionActionEvent(
                        eventId = command.eventId,
                        userId = command.userId,
                        sessionCommandType = SessionCommandType.EXPIRE_ALL_EXCEPT_CURRENT
                    )
                )
            }
            else
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = false,
                        errorMessage = "something went wrong"
                    )
                )
        }
        catch (e: Exception){
            kafkaProducerService.sendSessionResult(
                SessionResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "something went wrong: ${e.message}"
                )
            )
        }
    }

    suspend fun verify(command: SessionCommandEvent){
        try{
            val user = sessionService.checkSession(requireNotNull(command.sessionId))
            if(user > 0)
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        userId = user,
                        success = true
                    )
                )
            else{
                kafkaProducerService.sendSessionAction(
                    SessionActionEvent(
                        eventId = command.eventId,
                        userId = command.userId,
                        sessionCommandType = SessionCommandType.EXPIRE
                    )
                )
                kafkaProducerService.sendSessionResult(
                    SessionResultEvent(
                        eventId = command.eventId,
                        success = false,
                        errorMessage = "session expired"
                    )
                )
                }
        }
        catch (e: Exception){
            kafkaProducerService.sendSessionResult(
                SessionResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "something went wrong: ${e.message}"
                )
            )
        }
    }
}