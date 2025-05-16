package com.ivan.db_handler_service.config

import com.ivan.db_handler_service.kafka.BackupCommandEvent
import com.ivan.db_handler_service.kafka.DbBackupEvent
import com.ivan.db_handler_service.util.SafeJsonDeserializer
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.apache.kafka.clients.admin.NewTopic

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun consumerFactory(): ConsumerFactory<String, BackupCommandEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "db-backup-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to SafeJsonDeserializer::class.java,
            JsonDeserializer.VALUE_DEFAULT_TYPE to BackupCommandEvent::class.java.name,
            JsonDeserializer.TRUSTED_PACKAGES to "com.ivan.db_handler_service.kafka",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to "false",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to "60000",
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to "20000",
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to "300000"
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, DbBackupEvent> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.RETRIES_CONFIG to "3",
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to "1000",
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1"
        )
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, DbBackupEvent> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, BackupCommandEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, BackupCommandEvent>()
        factory.consumerFactory = consumerFactory()
        factory.setCommonErrorHandler(
            DefaultErrorHandler(
                FixedBackOff(1000L, 3)
            )
        )
        return factory
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun backupCommandsTopic(): NewTopic {
        return TopicBuilder.name("backup-commands")
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun backupResultsTopic(): NewTopic {
        return TopicBuilder.name("backup-results")
            .partitions(3)
            .replicas(1)
            .build()
    }
}