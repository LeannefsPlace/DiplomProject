package com.ivan.gateway_service.controller

import com.ivan.gateway_service.kafka.SkillDto
import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.service.SkillService
import com.ivan.gateway_service.service.UserService
import com.ivan.gateway_service.util.AuthenticatedUser
import com.ivan.gateway_service.util.auth_util.AuthUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val skillService: SkillService,
    private val userService: UserService
) {

    @GetMapping("/skills")
    suspend fun skillList() : ResponseEntity<List<SkillDto>> {
        return ResponseEntity.ok(
            skillService.listSkills()
        )
    }

    @GetMapping("/skills/find")
    suspend fun skillListQuery(
        @RequestParam("q", required = true) q : String,
    ) : ResponseEntity<List<SkillDto>> {
        return ResponseEntity.ok(
            skillService.listSkillsByType(q)
        )
    }

    @PostMapping("/skills")
    suspend fun addSkill(
        @RequestBody skillDto: SkillDto,
        @AuthenticatedUser authUser: AuthUser,
    ) : ResponseEntity<SkillDto> {
        return if(authUser.role == "ADMIN") ResponseEntity.ok(
            skillService.getSkill(
                skillService.addSkill(skillDto)
            )
        )
        else ResponseEntity(
            HttpStatus.FORBIDDEN
        )
    }

    @PostMapping("/skills/{skillId}/delete")
    suspend fun deleteSkill(
        @PathVariable("skillId") skillId: Int,
        @AuthenticatedUser authUser: AuthUser,
    ):ResponseEntity<Boolean> {
        return if(authUser.role == "ADMIN") return ResponseEntity.ok(
            skillService.deleteSkill(skillId)
        )
        else ResponseEntity(
            HttpStatus.FORBIDDEN
        )
    }

    @PostMapping("/user/{userId}/delete")
    suspend fun deleteUser(
        @AuthenticatedUser authUser: AuthUser,
        @PathVariable("userId") userId: Int,
    ) : ResponseEntity<Boolean> {
        return if(authUser.role == "ADMIN") return ResponseEntity.ok(
            userService.deleteUser(
                userId
            )
        )
        else ResponseEntity(
            HttpStatus.FORBIDDEN
        )
    }

    @GetMapping("/user")
    suspend fun listUser(
        @AuthenticatedUser authUser: AuthUser,
    ) : ResponseEntity<List<UserDto>> {
//        return if(authUser.role == "ADMIN") return ResponseEntity.ok(
            return ResponseEntity.ok(userService.getList())
//        )
//        else ResponseEntity(
//            HttpStatus.FORBIDDEN
//        )
    }

    @PostMapping("/user/{userId}")
    suspend fun editUser(
        @AuthenticatedUser authUser: AuthUser,
        @PathVariable("userId") userId: Int,
        @RequestParam("role", required = true) role: String,
    ) : ResponseEntity<Boolean> {
        return if(authUser.role == "ADMIN") return ResponseEntity.ok(
            userService.updateUser(
                userId = userId,
                role = role
            )
        )
        else ResponseEntity(
            HttpStatus.FORBIDDEN
        )
    }
}