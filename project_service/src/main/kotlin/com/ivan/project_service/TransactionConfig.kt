package com.ivan.project_service

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class TransactionConfig(
    private val entityManagerFactory: EntityManagerFactory
) {
    @Bean
    fun transactionTemplate(platformTransactionManager: PlatformTransactionManager): TransactionTemplate {
        return TransactionTemplate(platformTransactionManager).apply {
            isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        }
    }

    @Bean
    fun platformTransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}