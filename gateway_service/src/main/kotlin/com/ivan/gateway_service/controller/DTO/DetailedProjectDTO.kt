package com.ivan.gateway_service.controller.DTO

import com.ivan.gateway_service.kafka.ProjectDTO
import com.ivan.gateway_service.model.Project

data class DetailedProjectDTO(
    val projectId: Int,
    val projectDTO: ProjectDTO,
    val project: Project
)