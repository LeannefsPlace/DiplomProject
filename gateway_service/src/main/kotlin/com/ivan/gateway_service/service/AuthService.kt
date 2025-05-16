package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.*
import com.ivan.gateway_service.service.query.AuthQueryService
import com.ivan.gateway_service.service.query.UserQueryService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val authQueryService: AuthQueryService,
    private val userQueryService: UserQueryService
) {
    suspend fun validateSession(sessionId: String): Int? {
        val response = authQueryService.getAuthEvent(
            SessionCommandEvent(
                eventId = UUID.randomUUID().toString(),
                sessionCommandType = SessionCommandType.VERIFY,
                sessionId = sessionId,
                userId = null,
            )
        )

        return if (response.success) response.userId else null
    }

    suspend fun getUserFromSession(sessionId: String): UserDto? {
        val response = authQueryService.getAuthEvent(
            SessionCommandEvent(
                eventId = UUID.randomUUID().toString(),
                sessionCommandType = SessionCommandType.VERIFY,
                sessionId = sessionId,
                userId = null,
            )
        )

        if (response.success){
            val userResponse = userQueryService.getUserEvent(
                UserCommandEvent(
                    eventId = UUID.randomUUID().toString(),
                    commandType = UserCommandType.GET,
                    userId = response.userId
                )
            )
            return if (userResponse.success){
                userResponse.users!![0]
            } else null
        }
        else return null
    }

    suspend fun getUserFromId(userId: Int): UserDto? {

            val userResponse = userQueryService.getUserEvent(
                UserCommandEvent(
                    eventId = UUID.randomUUID().toString(),
                    commandType = UserCommandType.GET,
                    userId = userId,
                )
            )
            return if (userResponse.success){
                userResponse.users!![0]
            } else null
    }
}