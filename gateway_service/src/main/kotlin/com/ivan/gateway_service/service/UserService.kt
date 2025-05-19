package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.UserCommandEvent
import com.ivan.gateway_service.kafka.UserCommandType
import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.service.query.UserQueryService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userQueryService: UserQueryService) {
    suspend fun getUser(userId: Int): UserDto {
        val userResponse = userQueryService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.GET,
                userId = userId
            )
        )

        return userResponse.users?.get(0) ?: throw IllegalStateException("User not found")
    }

    suspend fun updateUserInfo(userDTO: UserDto): UserDto {
        val userResponse = userQueryService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.EDIT,
                userId = userDTO.id,
                email = userDTO.email,
                avatarUrl = userDTO.avatarUrl,
                fullName = userDTO.fullName,
                globalRole = userDTO.globalRole,
                skillIds = userDTO.skillIds
            )
        )

        return userResponse.users?.get(0) ?: throw IllegalStateException("User not found")
    }

    suspend fun updateUser(userId:Int, role:String) : Boolean{
        val userResponse = userQueryService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.EDIT,
                userId = userId,
                globalRole = role
            )
        )
        return userResponse.success
    }

    suspend fun deleteUser(userId: Int) : Boolean{
        val userResponse = userQueryService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.DELETE,
                userId = userId
            )
        )
        return userResponse.success
    }

    suspend fun getList(): List<UserDto> {
        val userResponse = userQueryService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.LIST
            )
        )

        return userResponse.users
            ?:throw IllegalStateException("List is empty")
    }
}