package com.ivan.user_service.service

import com.ivan.user_service.kafka.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${app.kafka.topics.user-results}") private val userResultsTopic: String,
    @Value("\${app.kafka.topics.skill-results}") private val skillResultsTopic: String,
    @Value("\${app.kafka.topics.user-actions}") private val userActionsTopic: String,
    @Value("\${app.kafka.topics.skill-actions}") private val skillActionsTopic: String
) {

    fun sendUserResult(result: UserResultEvent) {
        kafkaTemplate.send(userResultsTopic, result.eventId, result)
    }

    fun sendSkillResult(result: SkillResultEvent) {
        kafkaTemplate.send(skillResultsTopic, result.eventId, result)
    }

    fun sendUserAction(action: UserActionEvent) {
        kafkaTemplate.send(userActionsTopic, action.eventId, action)
    }

    fun sendSkillAction(result: SkillActionEvent) {
        kafkaTemplate.send(skillResultsTopic, result.eventId, result)
    }

}