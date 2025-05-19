package com.ivan.gateway_service.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.ivan.gateway_service.model.Project
import com.ivan.gateway_service.model.Task
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

//Producers

data class ProjectTaskCommandEvent(
    override val eventId: String,
    val commandType: ProjectTaskEventType,
    val task: TaskDTO? = null,
    val branch: BranchDTO? = null,
    val userId: Int? = null,
    val projectId: Int? = null,
    val branchId: String? = null,
) : ProjectTaskEvent()

data class ProjectCommandEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    val commandType: ProjectEventType,
    val userId: Int? = null,
    val projectId: Int? = null,
    val name: String? = null,
    val isActive: Boolean? = null,
    val avatarUrl: String? = null,
    val description: String? = null,
    val projectRole: ProjectRole? = null
) : ProjectEvent()

data class UserCommandEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    val commandType: UserCommandType,
    val userId: Int? = null,
    val login: String? = null,
    val email: String? = null,
    val passwordHash: String? = null,
    val avatarUrl: String? = null,
    val fullName: String? = null,
    val globalRole: String? = null,
    val skillIds: List<Int> = emptyList()
) : UserEvent()

data class SkillCommandEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    val commandType: SkillCommandType,
    val skillId: Int? = null,
    val name: String? = null,
    val type: String? = null
) : SkillEvent()

data class SessionCommandEvent(
    override val eventId: String,
    val sessionCommandType: SessionCommandType,
    val sessionId: String?,
    val userId: Int?
) : SessionEvent()

data class BackupCommandEvent(
    override val eventId: String,
    override val timestamp: Instant,
    val commandType: BackupCommandType,
    val backupFileName: String? = null,
    val dbType: String? = "postgresql"
) : DbBackupEvent()

//listeners

data class ProjectResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val projects: List<ProjectDTO>? = null,
    @JsonProperty val success: Boolean,
    @JsonProperty val errorMessage: String? = null,
    @JsonProperty val userId: Int? = null,
) : ProjectEvent()

data class ProjectActionEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val projectId: Int,
    @JsonProperty val actionType: ProjectActionType,
    @JsonProperty val userId: Int? = null,
) : ProjectEvent()

data class UserResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val users: List<UserDto>? = null,
    @JsonProperty val passwordHash: String? = null,
    @JsonProperty val success: Boolean,
    @JsonProperty val errorMessage: String? = null
) : UserEvent()

data class UserActionEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val actionType: String,
    @JsonProperty val userId: Int?,
    @JsonProperty val login: String?
) : UserEvent()

data class SkillActionEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val actionType: String,
    @JsonProperty val skillId: Int
) : SkillEvent()

data class SkillResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val skills: List<SkillDto>? = null,
    @JsonProperty val success: Boolean,
    @JsonProperty val errorMessage: String? = null
) : SkillEvent()

data class SessionResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val userId:Int? = null,
    @JsonProperty val success: Boolean,
    @JsonProperty val errorMessage: String? = null
) : SessionEvent()

data class SessionActionEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val userId: Int? = null,
    @JsonProperty val sessionCommandType: SessionCommandType? = null
) : SessionEvent()

data class BackupResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty override val timestamp: Instant = Instant.now(),
    @JsonProperty val status: BackupStatus,
    @JsonProperty val commandType: BackupCommandType,
    @JsonProperty val startTime: Instant,
    @JsonProperty val endTime: Instant,
    @JsonProperty val durationMs: Long,
    @JsonProperty val backupFiles: List<String>? = null,
    @JsonProperty val backupPath: String? = null,
    @JsonProperty val errorMessage: String? = null
) : DbBackupEvent()

data class ProjectTaskActionEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val projectId: Int
) : ProjectTaskEvent()

data class ProjectTaskResultEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val success: Boolean,
    @JsonProperty val errorMessage: String?,
    @JsonProperty val projects: List<Project>,
    @JsonProperty val tasks: List<TaskWithContextDto>
) : ProjectTaskEvent()

//DTO

data class TaskDTO(
    val taskId: String? = null,

    val parentId: String? = null,

    val title: String? = null,

    val description: String? = null,

    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING) val startDate: LocalDate? = null,

    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING) val endDate: LocalDate? = null,

    val done: Boolean = false,

    val hasProblem: Boolean = false,

    val problemMessage: String? = null,

    val skillId: Int? = null,

    val assignedTo: Int? = null,

    val file: String? = null,
)

data class BranchDTO(
    val branchId: String? = null,

    val name: String? = null,

    val active: Boolean = true
)

data class TaskStatistics(
    @JsonProperty var taskCount: Int,
    @JsonProperty var completedTasksCount: Int,
    @JsonProperty var delayedTasksCount: Int,
    @JsonProperty var problemTasksCount: Int
)

enum class ProjectTaskEventType{
    GET_PROJECT,
    TASKS_FOR_USER,
    CREATE_TASK,
    UPDATE_TASK,
    DELETE_TASK,
    CREATE_BRANCH,
    DELETE_BRANCH,
    UPDATE_BRANCH
}

data class TaskWithContextDto(
    val projectId: Int,
    val branchId: String,
    val task: Task
)

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

enum class ProjectRole {
    OWNER, MEMBER, MANAGER
}

data class UserDto(
    @JsonProperty val id: Int,
    @JsonProperty val login: String,
    @JsonProperty val email: String,
    @JsonProperty val fullName: String?,
    @JsonProperty val globalRole: String,
    @JsonProperty val skillIds: List<Int>,
    @JsonProperty val createdAt: String,
    @JsonProperty val avatarUrl: String?,
)

data class SkillDto(
    val id: Int,
    val name: String,
    val type: String,
    val userCount: Int? = null
)

enum class UserCommandType {
    CREATE, DELETE, LIST, EDIT, FIND_BY_SKILLS, GET
}

enum class SkillCommandType {
    CREATE, DELETE, LIST, FIND_BY_TYPE
}

enum class SessionCommandType {
    EXPIRE, EXPIRE_ALL, EXPIRE_ALL_EXCEPT_CURRENT, VERIFY
}

enum class BackupCommandType {
    CREATE, RESTORE, DELETE, LIST
}

enum class BackupStatus {
    SUCCESS, FAILED, IN_PROGRESS
}

sealed class UserEvent {
    abstract val eventId: String
}

sealed class SessionEvent {
    abstract val eventId: String
}

sealed class DbBackupEvent {
    abstract val eventId: String
    abstract val timestamp: Instant
}

sealed class ProjectEvent {
    abstract val eventId: String
}

sealed class SkillEvent {
    abstract val eventId: String
}

sealed class ProjectTaskEvent {
    abstract val eventId: String
}