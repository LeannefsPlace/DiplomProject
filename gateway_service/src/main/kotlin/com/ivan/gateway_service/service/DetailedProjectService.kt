package com.ivan.gateway_service.service

import com.ivan.gateway_service.controller.DTO.DetailedProjectDTO
import com.ivan.gateway_service.kafka.ProjectCommandEvent
import com.ivan.gateway_service.kafka.ProjectEventType
import com.ivan.gateway_service.kafka.ProjectTaskCommandEvent
import com.ivan.gateway_service.kafka.ProjectTaskEventType
import com.ivan.gateway_service.service.query.ProjectQueryService
import com.ivan.gateway_service.service.query.ProjectTasksQueryService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DetailedProjectService(
    private val projectQueryService: ProjectQueryService,
    private val projectTasksQueryService: ProjectTasksQueryService
) {
    suspend fun getProject(id:Int):DetailedProjectDTO{
        val header = projectQueryService.getProjectEvent(
            ProjectCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectEventType.GET,
                projectId = id
            )
        ).apply {
            if (!success) throw IllegalStateException("Error getting project header: ${errorMessage}")
        }
        val body = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.GET_PROJECT,
                projectId = id
            )
        ).apply {
            if (!success) throw IllegalStateException("Error getting project header: ${errorMessage}")
        }

        return DetailedProjectDTO(
            id,
            header.projects?.get(0)?:throw IllegalStateException("No project attached to header response"),
            body.projects.get(0)?:throw IllegalStateException("No project attached to body response")
        )
    }
}