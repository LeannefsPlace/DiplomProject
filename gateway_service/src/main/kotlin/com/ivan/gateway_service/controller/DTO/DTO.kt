package com.ivan.gateway_service.controller.DTO

import com.ivan.gateway_service.kafka.ProjectRole

data class UserProjectDTO(
    val id: Int? = null,
    val userId: Int,
    val projectId: Int,
    val projectRole: ProjectRole
)