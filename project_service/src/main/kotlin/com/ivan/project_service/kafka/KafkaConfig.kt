package com.ivan.project_service.kafka

import com.ivan.project_service.util.SafeJsonDeserializer
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,

    @Value("\${app.kafka.topics.project-commands}")
    private val projectCommandsTopic: String,
) {

    @Bean
    fun userCommandsTopic(): NewTopic {
        return TopicBuilder.name(projectCommandsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun projectConsumerFactory(): ConsumerFactory<String, ProjectCommandEvent> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "project-service-group"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        props[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = SafeJsonDeserializer::class.java
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = ProjectCommandEvent::class.java.name
        props[JsonDeserializer.TRUSTED_PACKAGES] = "com.ivan.project_service.kafka"
        props[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun projectCommandKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ProjectCommandEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ProjectCommandEvent>()
        factory.consumerFactory = projectConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }
}