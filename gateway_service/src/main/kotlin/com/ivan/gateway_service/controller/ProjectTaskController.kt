package com.ivan.gateway_service.controller

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.ivan.gateway_service.controller.DTO.DetailedProjectDTO
import com.ivan.gateway_service.kafka.BranchDTO
import com.ivan.gateway_service.kafka.ProjectRole
import com.ivan.gateway_service.kafka.TaskDTO
import com.ivan.gateway_service.service.DetailedProjectService
import com.ivan.gateway_service.service.ProjectTaskService
import com.ivan.gateway_service.util.AuthenticatedUser
import com.ivan.gateway_service.util.auth_util.AuthUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/project")
class ProjectTaskController(
    private val detailedProjectService: DetailedProjectService,
    private val projectTaskService: ProjectTaskService
) {
    @GetMapping("/{projectId}")
    suspend fun get(@PathVariable projectId: Int): ResponseEntity<DetailedProjectDTO> {
        return ResponseEntity.ok(
            detailedProjectService.getProject(projectId)
        )
    }

    @PostMapping("/{projectId}/branch")
    suspend fun addBranch(@PathVariable projectId: Int,
                          @RequestBody branchDTO: BranchDTO,
                          @AuthenticatedUser authUser: AuthUser): ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(
            projectTaskService.addBranchToProject(projectId, branchDTO).branches.any { b -> b.name == branchDTO.name }
        )
    }

    @PostMapping("/{projectId}/branch/edit")
    suspend fun editBranch(@PathVariable projectId: Int,
                           @RequestBody branchDTO: BranchDTO,
                           @AuthenticatedUser authUser: AuthUser): ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            projectTaskService.editBranch(projectId, branchDTO).branches.any { b -> b.branchId == branchDTO.branchId }
        )
    }

    @PostMapping("/{projectId}/branch/delete")
    suspend fun deleteBranch(@PathVariable projectId: Int,
                             @RequestBody branchDTO: BranchDTO,
                             @AuthenticatedUser authUser: AuthUser): ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            !projectTaskService.deleteBranch(projectId, branchDTO).branches.any { b -> b.branchId == branchDTO.branchId }
        )
    }

    @PostMapping("/{projectId}/branch/{branchId}")
    suspend fun addTask(
        @PathVariable projectId: Int,
        @PathVariable branchId: String,
        @RequestBody taskDTO: TaskDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            projectTaskService.addTaskToBranch(projectId, branchId, taskDTO).branches.any { b -> b.tasks.any{ t -> t.title == taskDTO.title } }
        )
    }

    @PostMapping("/{projectId}/branch/{branchId}/task/{taskId}")
    suspend fun editTask(
        @PathVariable projectId: Int,
        @PathVariable branchId: String,
        @PathVariable taskId: String,
        @RequestBody taskDTO: TaskDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            projectTaskService.editTaskInBranch(projectId, branchId, taskDTO.copy(taskId = taskId)).branches.any { b -> b.tasks.any{ t -> t.title == taskDTO.title } }
        )
    }

    @PostMapping("/{projectId}/branch/{branchId}/task/{taskId}/delete")
    suspend fun deleteTask(
        @PathVariable projectId: Int,
        @PathVariable branchId: String,
        @PathVariable taskId: String,
        @RequestBody taskDTO: TaskDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            !projectTaskService.deleteTaskInBranch(projectId, branchId, taskDTO.copy(taskId = taskId)).branches.any { b -> b.tasks.any{ t -> t.taskId == taskId } }
        )
    }

    @PostMapping("/{projectId}/branch/{branchId}/task/{taskId}/done")
    suspend fun setTaskDone(
        @PathVariable projectId: Int,
        @PathVariable branchId: String,
        @PathVariable taskId: String,
        @RequestBody taskDTO: TaskDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            projectTaskService.setTaskDone(projectId, branchId, taskId, taskDTO.file).branches.any { b -> b.tasks.any{ t -> t.taskId == taskId } }
        )
    }

    @PostMapping("/{projectId}/branch/{branchId}/task/{taskId}/problem")
    suspend fun setProblem(
        @PathVariable projectId: Int,
        @PathVariable branchId: String,
        @PathVariable taskId: String,
        @RequestBody taskDTO: TaskDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(
            projectTaskService.setTaskProblem(projectId, branchId, taskId, taskDTO.file, taskDTO.problemMessage).branches.any { b -> b.tasks.any{ t -> t.taskId == taskId } }
        )
    }
}