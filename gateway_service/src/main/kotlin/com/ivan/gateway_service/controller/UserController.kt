package com.ivan.gateway_service.controller

import com.ivan.gateway_service.kafka.ProjectDTO
import com.ivan.gateway_service.kafka.TaskWithContextDto
import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.service.ProjectInfoService
import com.ivan.gateway_service.service.ProjectTaskService
import com.ivan.gateway_service.service.UserService
import com.ivan.gateway_service.util.AuthenticatedUser
import com.ivan.gateway_service.util.auth_util.AuthUser
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val projectTaskService: ProjectTaskService,
    private val projectInfoService: ProjectInfoService
) {
    @PostMapping("/edit")
    suspend fun edit(
        @AuthenticatedUser authUser: AuthUser,
        @RequestBody userDto: UserDto
    ) : ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.updateUserInfo(userDto.copy(id = authUser.id)))
    }

    @GetMapping("")
    suspend fun getCurrentUser(
        @AuthenticatedUser authUser: AuthUser
    ) : ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getUser(authUser.id))
    }

    @GetMapping("/tasks")
    suspend fun getTasks(
        @AuthenticatedUser authUser: AuthUser
    ): ResponseEntity<List<TaskWithContextDto>>{
        return ResponseEntity.ok(projectTaskService.findTasksByUser(authUser.id))
    }
    @GetMapping("/projects")
    suspend fun getProjects(
        @AuthenticatedUser authUser: AuthUser
    ): ResponseEntity<List<ProjectDTO>>{
        return ResponseEntity.ok(projectInfoService.getForUser(authUser.id))
    }
}