package com.ivan.project_service.service

import com.ivan.project_service.kafka.*
import com.ivan.project_service.model.ProjectUser
import com.ivan.project_service.repostitory.ProjectRepository
import com.ivan.project_service.repostitory.ProjectUserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class ProjectUserService(
    private val projectRepository: ProjectRepository,
    private val transactionTemplate: TransactionTemplate,
    private val projectUserRepository: ProjectUserRepository,
    private val kafkaProducerService: KafkaProducerService,
    @Qualifier("jpaDispatcher") private val jpaDispatcher: CoroutineDispatcher
) {

    suspend fun assign(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    requireNotNull(event.userId)

                    requireNotNull(event.projectId)

                    if(projectUserRepository.existsByProjectIdAndUserId(event.userId, event.projectId)) throw IllegalArgumentException("User already assigned to such project")

                    var project = projectRepository.findById(event.projectId).orElseThrow()

                    projectUserRepository.save(
                        ProjectUser(
                            project = project,
                            userId = event.userId,
                            role = event.projectRole
                        )
                    )

                    project = projectRepository.findById(event.projectId).orElseThrow()

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            projects = mutableListOf(
                                project.toDTO()
                            ),
                            success = true,
                            errorMessage = null,
                            userId = event.userId
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while assigning user to project: ${e.message}",
                        )
                    )
                    println("Exception while assigning user to project: ${e.message}")
                    throw IllegalArgumentException("Error while assigning user to project", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun discharge(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    requireNotNull(event.userId)

                    requireNotNull(event.projectId)

                    requireNotNull(event.projectRole)

                    if(!projectUserRepository.existsByProjectIdAndUserId(event.userId, event.projectId)) throw IllegalArgumentException("User not assigned to such project")

                    projectUserRepository.deleteById(
                        projectUserRepository.findByProjectIdAndUserId(event.projectId, event.userId).orElseThrow().id
                    )

                    val project = projectRepository.findById(event.projectId).orElseThrow()

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            projects = mutableListOf(
                                project.toDTO()
                            ),
                            success = true,
                            errorMessage = null,
                            userId = event.userId
                        )
                    )

                    kafkaProducerService.sendProjectAction(
                        ProjectActionEvent(
                            eventId = event.eventId,
                            projectId = event.projectId,
                            actionType = ProjectActionType.DISCHARGE,
                            userId = event.userId
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while discharging user from project: ${e.message}",
                        )
                    )
                    println("Exception while discharging user from project: ${e.message}")
                    throw IllegalArgumentException("Error while discharging user from project", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun updateRole(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    requireNotNull(event.userId)

                    requireNotNull(event.projectId)

                    requireNotNull(event.projectRole)

                    if(!projectUserRepository.existsByProjectIdAndUserId(event.userId, event.projectId)) throw IllegalArgumentException("User not assigned to such project")

                    val toUpdate = projectUserRepository.findByProjectIdAndUserId(event.projectId, event.userId).orElseThrow()

                    toUpdate.role = event.projectRole

                    projectUserRepository.save(toUpdate)

                    val project = projectRepository.findById(event.projectId).orElseThrow()

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            projects = mutableListOf(
                                project.toDTO()
                            ),
                            success = true,
                            errorMessage = null,
                            userId = event.userId
                        )
                    )

                    kafkaProducerService.sendProjectAction(
                        ProjectActionEvent(
                            eventId = event.eventId,
                            projectId = event.projectId,
                            actionType = ProjectActionType.EDIT_ROLE,
                            userId = event.userId
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while editing user in project: ${e.message}",
                        )
                    )
                    println("Exception while editing user in project: ${e.message}")
                    throw IllegalArgumentException("Error while editing user in project", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }
}