package com.ivan.project_service.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.ivan.project_service.model.Project
import com.ivan.project_service.model.ProjectRole
import com.ivan.project_service.model.ProjectUser
import java.time.Instant
import java.util.*

sealed class ProjectEvent {
    abstract val eventId: String
}

data class ProjectCommandEvent(
    @JsonProperty("eventId") override val eventId: String = UUID.randomUUID().toString(),
    @JsonProperty("commandType") val commandType: ProjectEventType,
    @JsonProperty("userId") val userId: Int? = null,
    @JsonProperty("projectId") val projectId: Int? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("isActive") val isActive: Boolean? = null,
    @JsonProperty("avatarUrl") val avatarUrl: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("projectRole") val projectRole: ProjectRole? = null
) : ProjectEvent()

data class ProjectResultEvent(
    override val eventId: String,
    val projects: List<ProjectDTO>? = null,
    val success: Boolean,
    val errorMessage: String? = null,
    val userId: Int? = null,
) : ProjectEvent()

data class ProjectActionEvent(
    override val eventId: String,
    val projectId: Int,
    val actionType: ProjectActionType,
    val userId: Int? = null,
) : ProjectEvent()

data class ProjectDTO(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val createdAt: Instant? = null,
    val isActive: Boolean? = null,
    val avatarUrl: String? = null,
    val projectMembers: List<ProjectMemberDTO>? = null,
)

data class ProjectMemberDTO(
    val id: Int,
    val userId: Int,
    val role: ProjectRole
)

enum class ProjectEventType {
    CREATE, DELETE, UPDATE, LIST, GET, FOR_USER_LIST, ASSIGN, DISCHARGE, EDIT_ROLE
}

enum class ProjectActionType {
    CREATE, DELETE, UPDATE, DISCHARGE, EDIT_ROLE
}

fun Project.toDTO(): ProjectDTO =
    ProjectDTO(
        id = this.id,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        isActive = this.isActive,
        avatarUrl = this.avatarUrl,
        projectMembers = this.members.map {
            it.toDTO()
        }
    )

fun ProjectUser.toDTO(): ProjectMemberDTO =
    ProjectMemberDTO(
        id = this.id,
        userId = this.userId?:throw IllegalArgumentException("Failed to process userId"),
        role = this.role?:throw IllegalArgumentException("Failed to process projectRole")
    )