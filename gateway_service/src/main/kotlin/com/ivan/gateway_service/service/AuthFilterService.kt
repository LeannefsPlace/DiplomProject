package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.util.auth_util.AuthUser
import com.ivan.gateway_service.util.auth_util.SessionStorage
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class AuthFilterService(
    private val sessionStorage: SessionStorage,
    private val authService: AuthService,
    private val projectInfoService: ProjectInfoService
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // Пропускаем публичные эндпоинты
        if (httpRequest.requestURI.startsWith("/public")) {
            chain.doFilter(request, response)
            return
        }

        // Извлекаем JWT
        val token = httpRequest.getHeader("Authorization")?.removePrefix("Bearer ") ?: run {
            sendError(httpResponse, 401, "Token missing")
            return
        }

        // Проверяем сессию
        var authUser = sessionStorage.getUserByToken(token)
        if (authUser == null) {
            val user:UserDto?
            runBlocking {
                user = authService.getUserFromSession(token)
            }
            if (user == null) {
                sendError(httpResponse, 403, "Invalid or expired session")
                return
            }

            runBlocking {
                authUser = AuthUser(
                    id = user.id,
                    login = user.login,
                    role = user.globalRole,
                    projectRoles = projectInfoService.getProjectsAuthForUser(user.id),
                    lastActive = Instant.now()
                )
            }

            sessionStorage.addOrUpdateSession(token, authUser?:throw Exception ("something went wrong, authUser was null"))
        }

        // Добавляем пользователя в атрибуты запроса
        httpRequest.setAttribute("authUser", authUser)
        chain.doFilter(request, response)
    }

    private fun sendError(response: HttpServletResponse, code: Int, message: String) {
        if (!response.isCommitted) {
            response.status = code
            response.writer.write("""{"error": "$message"}""")
            response.contentType = "application/json"
        }
    }
}