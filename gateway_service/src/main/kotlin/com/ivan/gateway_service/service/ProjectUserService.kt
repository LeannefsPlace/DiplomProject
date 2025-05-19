package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.ProjectCommandEvent
import com.ivan.gateway_service.kafka.ProjectDTO
import com.ivan.gateway_service.kafka.ProjectEventType
import com.ivan.gateway_service.kafka.ProjectRole
import com.ivan.gateway_service.service.query.ProjectQueryService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectUserService(private val projectQueryService: ProjectQueryService) {

    suspend fun assignUserToProject(projectId: Int, userId: Int, projectRole: ProjectRole): ProjectDTO {
        val projectResponse = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.ASSIGN,
                userId = userId,
                projectId = projectId,
                projectRole = projectRole
            )
        )

        return if (projectResponse.success) projectResponse.projects?.get(0)?: throw IllegalStateException("No project found in response")
            else throw IllegalStateException("Exception while fetching project: ${projectResponse.errorMessage}")
    }

    suspend fun dischargeUserFromProject(projectId: Int, userId: Int): ProjectDTO {
        val projectResponse = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.DISCHARGE,
                userId = userId,
                projectId = projectId
            )
        )

        return if (projectResponse.success) projectResponse.projects?.get(0)?: throw IllegalStateException("No project found in response")
        else throw IllegalStateException("Exception while fetching project: ${projectResponse.errorMessage}")
    }

    suspend fun editUserRoleForProject(projectId: Int, userId: Int, projectRole: ProjectRole): ProjectDTO {
        val projectResponse = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.EDIT_ROLE,
                userId = userId,
                projectId = projectId,
                projectRole = projectRole
            )
        )

        return if (projectResponse.success) projectResponse.projects?.get(0)?: throw IllegalStateException("No project found in response")
        else throw IllegalStateException("Exception while fetching project: ${projectResponse.errorMessage}")
    }
}