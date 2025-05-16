package com.ivan.gateway_service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Duration
import java.util.*

@SpringBootApplication
class GatewayServiceApplication

fun main(args: Array<String>) {
    runApplication<GatewayServiceApplication>(*args)
}