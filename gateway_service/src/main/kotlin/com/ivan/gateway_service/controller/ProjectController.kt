package com.ivan.gateway_service.controller

import com.ivan.gateway_service.controller.DTO.UserProjectDTO
import com.ivan.gateway_service.kafka.ProjectDTO
import com.ivan.gateway_service.kafka.ProjectRole
import com.ivan.gateway_service.service.ProjectInfoService
import com.ivan.gateway_service.service.ProjectUserService
import com.ivan.gateway_service.util.AuthenticatedUser
import com.ivan.gateway_service.util.auth_util.AuthUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectInfoService: ProjectInfoService,
    private val projectUserService: ProjectUserService
) {

    @GetMapping("")
    suspend fun listProjects(
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<List<ProjectDTO>> {

        return if (authUser.role == "ADMIN") ResponseEntity.ok(projectInfoService.listProjects()?:emptyList())
        else ResponseEntity(HttpStatus.FORBIDDEN)
    }

    @PostMapping("")
    suspend fun createProject(
        @AuthenticatedUser authUser: AuthUser,
        @RequestBody projectDTO: ProjectDTO
    ) : ResponseEntity<ProjectDTO> {
        return ResponseEntity.ok(projectInfoService.newProject(
            projectDTO,
            authUser.id
        ))
    }

    @GetMapping("/list")
    suspend fun listProjectsForUser(
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<List<ProjectDTO>> {
        println(authUser.id)
        return ResponseEntity.ok(projectInfoService.getForUser(authUser.id))
    }

    @PostMapping("/{projectId}/delete")
    suspend fun deleteProject(
        @AuthenticatedUser authUser: AuthUser,
        @PathVariable("projectId") projectId: Int
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)
        return ResponseEntity.ok(projectInfoService.deleteProject(projectId, authUser.id))
    }

    @PostMapping("/{projectId}/edit")
    suspend fun editProject(
        @AuthenticatedUser authUser: AuthUser,
        @PathVariable("projectId") projectId: Int,
        @RequestBody projectDTO: ProjectDTO
    ) : ResponseEntity<Boolean> {
        val prevProject = projectInfoService.get(projectId)
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(projectInfoService.updateProject(
            name = projectDTO.name?:prevProject.name?:"",
            description = projectDTO.description?:prevProject.description?:"",
            active = projectDTO.isActive?:prevProject.isActive?:false,
            userId = authUser.id,
            projectId = projectId,
            avatarUrl = projectDTO.avatarUrl?:prevProject.avatarUrl
        ))
    }

    @PostMapping("/{projectId}/users")
    suspend fun assignUserForProject(
        @PathVariable("projectId") projectId: Int,
        @RequestBody userProjectDTO: UserProjectDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean>{

        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)

        val project = projectUserService.assignUserToProject(
            projectId = projectId,
            userId = userProjectDTO.userId,
            projectRole = userProjectDTO.projectRole,
        )

        return if (project.projectMembers?.any { u -> u.userId == userProjectDTO.userId && u.role == userProjectDTO.projectRole } == true)
            ResponseEntity.ok(true)
        else ResponseEntity.ok(false)
    }

    @PostMapping("/{projectId}/users/{userId}")
    suspend fun dischargeUserFromProject(
        @PathVariable("projectId") projectId: Int,
        @PathVariable("userId") userId: Int,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)})
            return ResponseEntity(HttpStatus.FORBIDDEN)

        val project = projectUserService.dischargeUserFromProject(
            projectId = projectId,
            userId = userId
        )

        return if (project.projectMembers?.any { u -> u.userId == userId } == false)
            ResponseEntity.ok(true)
        else ResponseEntity.ok(false)
    }

    @PostMapping("/{projectId}/users/{userId}/edit")
    suspend fun editUserFromProject(
        @PathVariable("projectId") projectId: Int,
        @PathVariable("userId") userId: Int,
        @RequestBody userDTO: UserProjectDTO,
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<Boolean> {
        if (!authUser.projectRoles.any{u -> u.projectId == projectId && (u.projectRole == ProjectRole.OWNER || u.projectRole == ProjectRole.MANAGER)} && userDTO.userId != userId)
            return ResponseEntity(HttpStatus.FORBIDDEN)

        val project = projectUserService.editUserRoleForProject(
            projectId = projectId,
            userId = userId,
            projectRole = userDTO.projectRole,
        )

        return if (project.projectMembers?.any { u -> u.userId == userId && u.role == userDTO.projectRole } == true)
            ResponseEntity.ok(true)
        else ResponseEntity.ok(false)
    }
}