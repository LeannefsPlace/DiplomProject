package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.ProjectCommandEvent
import com.ivan.gateway_service.kafka.ProjectDTO
import com.ivan.gateway_service.kafka.ProjectEventType
import com.ivan.gateway_service.service.query.ProjectQueryService
import com.ivan.gateway_service.util.auth_util.AuthProject
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectInfoService (
    val projectQueryService: ProjectQueryService
) {
    suspend fun listProjects() : List<ProjectDTO> {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.LIST
            )
        )

        return projects.projects?:throw IllegalStateException("Projects is null")
    }

    suspend fun getForUser(userId:Int) : List<ProjectDTO> {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.FOR_USER_LIST,
                userId = userId
            )
        )

        return projects.projects?:throw IllegalStateException("Projects is null")
    }

    suspend fun get(projectId:Int) : ProjectDTO {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.GET,
                projectId = projectId
            )
        )

        return projects.projects?.get(0)?:throw IllegalStateException("Project is null")
    }

    suspend fun newProject(
        projectDto: ProjectDTO,
        userId: Int
    ) : ProjectDTO {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.CREATE,
                name = projectDto.name,
                description = projectDto.description,
                avatarUrl = projectDto.avatarUrl,
                isActive = projectDto.isActive,
                userId = userId
            )
        )

        return projects.projects?.get(0)?:throw IllegalStateException("Project is null")
    }

    suspend fun updateProject(
        projectId: Int,
        name: String,
        description: String,
        active: Boolean,
        userId: Int,
        avatarUrl: String?,
    ) : Boolean {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.UPDATE,
                name = name,
                isActive = active,
                description = description,
                userId = userId,
                avatarUrl = avatarUrl,
                projectId = projectId
            )
        )

        return projects.success
    }

    suspend fun deleteProject(
        projectId: Int,
        userId: Int
    ) : Boolean {
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                userId = userId,
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.DELETE,
                projectId = projectId
            )
        )

        return projects.success
    }

    suspend fun getProjectsAuthForUser(userId:Int) : List<AuthProject>{
        val projects = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.FOR_USER_LIST,
                userId = userId
            )
        ).projects

        return projects?.map { project -> AuthProject(
            projectId = project.id?:throw IllegalStateException("Project Id required but not found"),
            projectRole = project.projectMembers?.firstOrNull { pm -> pm.userId == userId }?.role?:throw IllegalStateException("User Role expected but not found")
        )}.orEmpty()
    }

}