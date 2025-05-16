package com.ivan.gateway_service.controller

import com.ivan.gateway_service.JwtConfig
import com.ivan.gateway_service.kafka.UserDto
import com.ivan.gateway_service.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
    private val jwtConfig: JwtConfig
) {
    @GetMapping("/auth")
    suspend fun authorize(
        @RequestParam(value = "access_token", required = true) accessToken: String,
    ): ResponseEntity<String> {
        val oleg = jwtConfig.generateToken(accessToken)
        println(oleg)
        return ResponseEntity.ok().apply {
            body(oleg)
        }.build()
    }

    @GetMapping("/check")
    suspend fun check(
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok().apply {
            body(
                 authService.getUserFromId(userId.toInt())
            )
        }.build()
    }
}