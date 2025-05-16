package com.ivan.db_handler_service.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoConfig {

    @Bean
    fun mongoClient(
        @Value("\${spring.data.mongodb.host}") host: String,
        @Value("\${spring.data.mongodb.port}") port: Int
    ): MongoClient {
        val connectionString = "mongodb://$host:$port"
        return MongoClients.create(connectionString)
    }

    @Bean
    fun mongoDatabase(
        mongoClient: MongoClient,
        @Value("\${spring.data.mongodb.database}") dbName: String
    ): MongoDatabase {
        return mongoClient.getDatabase(dbName)
    }
}