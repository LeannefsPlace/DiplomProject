package com.ivan.db_handler_service.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun coroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}