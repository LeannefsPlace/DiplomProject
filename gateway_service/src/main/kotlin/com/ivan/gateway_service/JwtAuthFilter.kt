package com.ivan.gateway_service

import com.ivan.gateway_service.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter

@Service
class JwtSessionAuthFilter(
    private val jwtConfig: JwtConfig,
    private val sessionService: AuthService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }
        runBlocking {
            try {
                val token = authHeader.substring(7)
                if (!jwtConfig.validateToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT")
                    return@runBlocking
                }
                val sessionId = jwtConfig.extractSessionId(token)
                    ?: throw IllegalArgumentException("No sessionId in token")
                val userId = sessionService.validateSession(sessionId)
                    ?: throw Exception("Session expired")
                val auth = UsernamePasswordAuthenticationToken(
                    userId,
                    null
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return@runBlocking
            }
        }
        chain.doFilter(request, response)
    }
}