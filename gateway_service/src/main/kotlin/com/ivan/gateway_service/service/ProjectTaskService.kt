package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.*
import com.ivan.gateway_service.model.Project
import com.ivan.gateway_service.service.query.ProjectTasksQueryService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectTaskService(private val projectTasksQueryService: ProjectTasksQueryService) {
    suspend fun addBranchToProject(projectId: Int, branchDTO: BranchDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.CREATE_BRANCH,
                branch = branchDTO,
                projectId = projectId
            )
        )

        if (!response.success) throw IllegalStateException("Failed to add branch to project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun editBranch(projectId: Int, branchDTO: BranchDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.UPDATE_BRANCH,
                branch = branchDTO,
                projectId = projectId
            )
        )

        if (!response.success) throw IllegalStateException("Failed to edit branch in project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun deleteBranch(projectId: Int, branchDTO: BranchDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.DELETE_BRANCH,
                branch = branchDTO,
                projectId = projectId
            )
        )

        if (!response.success) throw IllegalStateException("Failed to delete branch for project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun addTaskToBranch(projectId: Int, branchId:String, task:TaskDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.CREATE_TASK,
                projectId = projectId,
                branchId = branchId,
                task = task
            )
        )

        if (!response.success) throw IllegalStateException("Failed to add task for branch for project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun editTaskInBranch(projectId: Int, branchId:String, task:TaskDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.UPDATE_TASK,
                projectId = projectId,
                branchId = branchId,
                task = task
            )
        )

        if (!response.success) throw IllegalStateException("Failed to update task for branch for project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun deleteTaskInBranch(projectId: Int, branchId:String, task:TaskDTO): Project {
        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.DELETE_TASK,
                projectId = projectId,
                branchId = branchId,
                task = task
            )
        )

        if (!response.success) throw IllegalStateException("Failed to delete task for branch for project ${response.errorMessage}")

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun setTaskDone(projectId: Int, branchId:String, taskId:String, file:String?): Project {
        val task = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.GET_PROJECT,
                projectId = projectId
            )
        ).apply {
            if (!success) throw IllegalStateException("Problem while fetching data $errorMessage")
        }.projects.get(0).branches.filter { b -> b.branchId == branchId }.first().tasks.filter { t -> t.taskId == taskId }.first()

        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.UPDATE_TASK,
                projectId = projectId,
                branchId = branchId,
                task = TaskDTO(
                    taskId = task.taskId,
                    parentId = task.parentId,
                    title = task.title,
                    description = task.description,
                    startDate = task.startDate,
                    endDate = task.endDate,
                    done = true,
                    hasProblem = task.hasProblem,
                    problemMessage = task.problemMessage,
                    skillId = task.skillId,
                    assignedTo = task.assignedTo,
                    file = file
                )
            )
        ).apply {
            if (success) throw IllegalStateException("Problem while fetching data $errorMessage")
        }

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun setTaskProblem(projectId: Int, branchId:String, taskId:String, file: String?, message:String?): Project {
        val task = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.GET_PROJECT,
                projectId = projectId
            )
        ).apply {
            if (!success) throw IllegalStateException("Problem while fetching data $errorMessage")
        }.projects.get(0).branches.filter { b -> b.branchId == branchId }.first().tasks.filter { t -> t.taskId == taskId }.first()

        val response = projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.UPDATE_TASK,
                projectId = projectId,
                branchId = branchId,
                task = TaskDTO(
                    taskId = task.taskId,
                    parentId = task.parentId,
                    title = task.title,
                    description = task.description,
                    startDate = task.startDate,
                    endDate = task.endDate,
                    done = task.done,
                    hasProblem = true,
                    problemMessage = message,
                    skillId = task.skillId,
                    assignedTo = task.assignedTo,
                    file = file
                )
            )
        ).apply {
            if (success) throw IllegalStateException("Problem while fetching data $errorMessage")
        }

        return response.projects[0] ?:throw IllegalStateException("no project connected to response")
    }

    suspend fun findTasksByUser(userId:Int) : List<TaskWithContextDto>{
        val response =projectTasksQueryService.getProjectTaskEvent(
            ProjectTaskCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = ProjectTaskEventType.TASKS_FOR_USER,
                userId = userId
            )
        ).apply {
            if (!success) throw IllegalStateException("Problem while fetching data $errorMessage")
        }

        return response.tasks
    }
}