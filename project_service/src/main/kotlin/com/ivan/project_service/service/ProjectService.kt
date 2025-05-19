package com.ivan.project_service.service

import com.ivan.project_service.kafka.*
import com.ivan.project_service.model.Project
import com.ivan.project_service.model.ProjectRole
import com.ivan.project_service.model.ProjectUser
import com.ivan.project_service.repostitory.ProjectRepository
import com.ivan.project_service.repostitory.ProjectUserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val transactionTemplate: TransactionTemplate,
    private val projectUserRepository: ProjectUserRepository,
    private val kafkaProducerService: KafkaProducerService,
    @Qualifier("jpaDispatcher") private val jpaDispatcher: CoroutineDispatcher
) {
    suspend fun createProject(event: ProjectCommandEvent) : Project =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val project = projectRepository.save(
                        Project(
                            name = event.name,
                            description = event.description,
                            avatarUrl = event.avatarUrl
                        )
                    )

                    projectUserRepository.save(
                        ProjectUser(
                            project = project,
                            userId = event.userId
                                ?: throw IllegalArgumentException("User ID required to add owner. Creation Failed."),
                            role = ProjectRole.OWNER
                        )
                    )

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            projects = mutableListOf(project.toDTO()),
                            success = true,
                            errorMessage = null
                        )
                    )

                    kafkaProducerService.sendProjectAction(ProjectActionEvent(
                        eventId = event.eventId,
                        projectId = project.id,
                        actionType = ProjectActionType.CREATE,
                        userId = event.userId
                    ))

                    project
                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while creating project: ${e.message}",
                        )
                    )
                    println("Exception while creating project: ${e.message}")
                    throw IllegalArgumentException("Error while creating project", e)
                }

            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun updateProject(event: ProjectCommandEvent) : Project =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {
                    requireNotNull(event.projectId)

                    requireNotNull(event.userId)

                    if(projectUserRepository.findByProjectIdAndUserId(event.projectId, event.userId).orElseThrow()
                        .role != ProjectRole.OWNER)
                            throw IllegalStateException("User has no authority for project ${event.projectId}")

                    val prevProject = projectRepository.findById(event.projectId).orElseThrow()

                    val project = projectRepository.save(
                        Project(
                            id = event.projectId,
                            name = event.name?:prevProject.name,
                            description = event.description?:prevProject.description,
                            avatarUrl = event.avatarUrl?:prevProject.avatarUrl,
                            isActive = event.isActive?:prevProject.isActive,
                            createdAt = prevProject.createdAt,
                            members = projectUserRepository.findAllByProject_Id(event.projectId)
                        )
                    )

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            projects = mutableListOf(project.toDTO()),
                            success = true,
                            errorMessage = null
                        )
                    )

                    kafkaProducerService.sendProjectAction(ProjectActionEvent(
                        eventId = event.eventId,
                        projectId = project.id,
                        actionType = ProjectActionType.UPDATE
                    ))

                    project
                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while updating project: ${e.message}",
                        )
                    )
                    println("Exception while updating project: ${e.message}")
                    throw IllegalArgumentException("Error while updating project", e)
                }

            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun deleteProject(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    requireNotNull(event.projectId)

                    requireNotNull(event.userId)

                    if(projectUserRepository.findByProjectIdAndUserId(event.projectId, event.userId).orElseThrow()
                            .role != ProjectRole.OWNER)
                        throw IllegalStateException("User has no authority for project ${event.projectId}")

                    projectRepository.deleteById(event.projectId)

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            success = true,
                            errorMessage = null
                        )
                    )

                    kafkaProducerService.sendProjectAction(ProjectActionEvent(
                        eventId = event.eventId,
                        projectId = event.projectId,
                        actionType = ProjectActionType.DELETE
                    ))

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while deleting projects: ${e.message}",
                        )
                    )
                    println("Exception while deleting projects: ${e.message}")
                    throw IllegalArgumentException("Error while deleting projects", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun projectList(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            projects = projectRepository.findAll().map{
                                    project -> project.toDTO()
                                },
                            success = true,
                            errorMessage = null
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while finding projects: ${e.message}",
                        )
                    )
                    println("Exception while finding projects: ${e.message}")
                    throw IllegalArgumentException("Error while finding projects", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun projectListForUser(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    requireNotNull(event.userId)

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            projects = projectRepository.findAllByUserId(event.userId).map{
                                    project -> project.toDTO()
                            },
                            success = true,
                            errorMessage = null
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while finding projects: ${e.message}",
                        )
                    )
                    println("Exception while finding projects: ${e.message}")
                    throw IllegalArgumentException("Error while finding projects", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }

    suspend fun getProject(event: ProjectCommandEvent) =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {

                try {

                    requireNotNull(event.projectId)

                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            userId = event.userId,
                            projects = mutableListOf(projectRepository.findById(event.projectId).orElseThrow().toDTO()),
                            success = true,
                            errorMessage = null
                        )
                    )

                } catch (e: Exception) {
                    kafkaProducerService.sendProjectResult(
                        ProjectResultEvent(
                            eventId = event.eventId,
                            success = false,
                            errorMessage = "Exception while finding project: ${e.message}",
                        )
                    )
                    println("Exception while finding project: ${e.message}")
                    throw IllegalArgumentException("Error while finding project", e)
                }
            } ?: throw IllegalStateException("Transaction failed")
        }
}