package com.ivan.project_task_service.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.ivan.project_task_service.model.Branch
import com.ivan.project_task_service.model.Project
import com.ivan.project_task_service.model.Task
import com.ivan.project_task_service.model.TaskStatistics
import com.ivan.project_task_service.repository.TaskWithContextDto
import java.time.LocalDate
import java.util.*

sealed class ProjectEvent {
    abstract val eventId: String
}

sealed class ProjectTaskEvent {
    abstract val eventId: String
}

sealed class ProjectTaskCommandEvent(
    @JsonProperty override val eventId: String,
    @JsonProperty val commandType: ProjectTaskEventType,
    @JsonProperty val task: TaskDTO?,
    @JsonProperty val branch: BranchDTO,
    @JsonProperty val userId: Int?,
    @JsonProperty val projectId: Int?,
    @JsonProperty val branchId: String?
) : ProjectTaskEvent()

data class ProjectTaskResultEvent(
    override val eventId: String,
    val success: Boolean,
    val errorMessage: String?,
    val projects: List<Project>,
    val tasks: List<TaskWithContextDto>
) : ProjectTaskEvent()

data class ProjectTaskActionEvent(
    override val eventId: String,
    val projectId: Int
) : ProjectTaskEvent()

data class TaskDTO(
    @JsonProperty val taskId: String? = null,

    @JsonProperty val parentId: String? = null,

    @JsonProperty val title: String? = null,

    @JsonProperty val description: String? = null,

    @JsonProperty val startDate: LocalDate? = null,

    @JsonProperty val endDate: LocalDate? = null,

    @JsonProperty val done: Boolean = false,

    @JsonProperty val hasProblem: Boolean = false,

    @JsonProperty val problemMessage: String? = null,

    @JsonProperty val skillId: Int? = null,

    @JsonProperty val assignedTo: Int? = null,

    @JsonProperty val file: String? = null,
){
    fun toTask():Task = Task(
        taskId = taskId?: UUID.randomUUID().toString(),
        parentId = parentId,
        title = title?: throw IllegalArgumentException("title"),
        description = description?:  throw IllegalArgumentException("description"),
        startDate = startDate?: throw IllegalArgumentException("startDate"),
        endDate = endDate?: throw IllegalArgumentException("endDate"),
        done = done,
        hasProblem = hasProblem,
        problemMessage = problemMessage,
        skillId = skillId,
        assignedTo = assignedTo,
        file = file
    )
}

data class BranchDTO(
    @JsonProperty val branchId: String? = null,

    @JsonProperty val name: String? = null,

    @JsonProperty val active: Boolean = true
){
    fun toBranch(): Branch = Branch(
        branchId = branchId?: UUID.randomUUID().toString(),
        name = name?: throw IllegalArgumentException("name"),
        active = active,
        tasks = emptyList(),
        statistics = TaskStatistics(
            taskCount = 0,
            completedTasksCount = 0,
            delayedTasksCount = 0,
            problemTasksCount = 0
        )
    )
}

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

data class ProjectActionEvent(
    @JsonProperty("event_id")
    override val eventId: String,

    @JsonProperty("project_id")
    val projectId: Int,

    @JsonProperty("action_type")
    val actionType: ProjectActionType,

    @JsonProperty("user_id")
    val userId: Int? = null,
) : ProjectEvent()

enum class ProjectActionType {
    CREATE, DELETE, UPDATE, DISCHARGE, EDIT_ROLE
}

data class UserActionEvent(
    val eventId: String,
    val actionType: String,
    val userId: Int?,
    val login: String?
)

data class SkillActionEvent(
    val eventId: String,
    val actionType: String,
    val skillId: Int
)

