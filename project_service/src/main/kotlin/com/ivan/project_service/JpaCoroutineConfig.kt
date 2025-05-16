package com.ivan.project_service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class JpaCoroutineConfig(
    @Value("\${spring.jpa.coroutine.pool.size:20}") private val poolSize: Int
) {
    @Bean
    fun jpaDispatcher(): CoroutineDispatcher {
        return Executors.newFixedThreadPool(poolSize).asCoroutineDispatcher()
    }

    @PreDestroy
    fun cleanup() {
        (jpaDispatcher() as ExecutorCoroutineDispatcher).close()
    }
}