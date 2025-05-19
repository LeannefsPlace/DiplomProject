package com.ivan.gateway_service.util.auth_util

import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.service.AuthService
import com.ivan.gateway_service.service.ProjectInfoService
import com.ivan.gateway_service.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Component
class SessionStorage(
    val projectInfoService: ProjectInfoService,
    private val userService: UserService,
    private val authService: AuthService,
) {
    private val activeSessions = ConcurrentHashMap<String, AuthUser>()

    fun updateTokensByUserId(userId: Int) {
        val iterator = activeSessions.entries.iterator()
        while (iterator.hasNext()) {
            val (token, authUser) = iterator.next()
            if (authUser.id == userId) {
                runBlocking {
                    try {
                        authService.validateSession(token)?:throw Exception("not found token")
                        val user:UserDto = userService.getUser(userId)
                        authUser.copy(
                            id = userId,
                            login = user.login,
                            role = user.globalRole,
                            projectRoles = projectInfoService.getProjectsAuthForUser(userId),
                            lastActive = Instant.now()
                        )
                    }catch(ex:Exception) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    fun updateSessionByUserId(userId: Int) {
        activeSessions.replaceAll { _, authUser ->
            if (authUser.id == userId) {
                runBlocking {
                    authUser.copy(
                        projectRoles = projectInfoService.getProjectsAuthForUser(userId)
                    )
                }
            } else {
                authUser
            }
        }
    }

    fun addOrUpdateSession(token: String, user: AuthUser) {
        activeSessions[token] = user
    }

    fun getUserByToken(token: String): AuthUser? {
        return activeSessions[token]?.also {
            it.lastActive = Instant.now()
        }
    }

    fun updateSession(token: String, user: AuthUser) {
        activeSessions[token] = user
    }

    fun removeSession(token: String) {
        activeSessions.remove(token)
    }

    fun removeAllSessionsForUser(userId: Int): Mono<Void> {
        return Mono.fromRunnable {
            activeSessions.entries.removeIf { it.value.id == userId }
        }
    }

    @Scheduled(fixedRate = 3600000)
    fun cleanupInactiveSessions() {
        val cutoff = Instant.now().minus(2, ChronoUnit.HOURS)
        activeSessions.entries.removeIf { it.value.lastActive.isBefore(cutoff) }
    }
}