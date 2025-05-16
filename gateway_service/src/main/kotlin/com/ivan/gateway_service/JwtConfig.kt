package com.ivan.gateway_service

import com.ivan.gateway_service.service.AuthService
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import java.time.Duration

@Component
class JwtConfig(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Duration,
    private val authService: AuthService
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    suspend fun generateToken(sessionId: String): String {
        return Jwts.builder()
            .setSubject(authService.getUserFromSession(sessionId)!!.id.toString())
            .claim("sessionId", sessionId)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration.toMillis()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    suspend fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun extractSessionId(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .get("sessionId", String::class.java)
        } catch (e: Exception) {
            null
        }
    }
}