package com.ivan.gateway_service

import com.ivan.gateway_service.service.AuthFilterService
import com.ivan.gateway_service.service.AuthService
import com.ivan.gateway_service.service.ProjectInfoService
import com.ivan.gateway_service.util.auth_util.SessionStorage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class GatewayServiceApplication

fun main(args: Array<String>) {
    runApplication<GatewayServiceApplication>(*args)
}

@Bean
fun filter(
    authService: AuthService,
    sessionStorage: SessionStorage,
    projectInfoService: ProjectInfoService
): FilterRegistrationBean<AuthFilterService>{
    val registrationBean = FilterRegistrationBean<AuthFilterService>()
    registrationBean.filter = AuthFilterService(
        sessionStorage = sessionStorage,
        authService = authService,
        projectInfoService = projectInfoService
    )
    registrationBean.order = Ordered.HIGHEST_PRECEDENCE
    return registrationBean
}

@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
