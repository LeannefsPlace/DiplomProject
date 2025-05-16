package com.ivan.project_service.service

import com.ivan.project_service.kafka.ProjectCommandEvent
import com.ivan.project_service.kafka.ProjectEvent
import com.ivan.project_service.kafka.ProjectEventType
import com.ivan.project_service.repostitory.ProjectRepository
import com.ivan.project_service.repostitory.ProjectUserRepository
import org.apache.kafka.common.requests.DeleteAclsResponse.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class KafkaMessagingService(
    private val projectService: ProjectService,
    private val projectUserService: ProjectUserService
) {

    @KafkaListener(
        topics = ["\${app.kafka.topics.project-commands}"],
        containerFactory = "projectCommandKafkaListenerContainerFactory"
    )
    suspend fun handleUserCommands(command: ProjectCommandEvent, ack: Acknowledgment) {
        try {
            when (command.commandType) {
                ProjectEventType.CREATE -> projectService.createProject(command)
                ProjectEventType.DELETE -> projectService.deleteProject(command)
                ProjectEventType.UPDATE -> projectService.updateProject(command)
                ProjectEventType.LIST -> projectService.projectList(command)
                ProjectEventType.GET -> projectService.getProject(command)
                ProjectEventType.FOR_USER_LIST -> projectService.projectListForUser(command)
                ProjectEventType.ASSIGN -> projectUserService.assign(command)
                ProjectEventType.DISCHARGE -> projectUserService.discharge(command)
                ProjectEventType.EDIT_ROLE -> projectUserService.updateRole(command)
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing project command: ${e.message}", e)
            ack.acknowledge()
        }
    }
}