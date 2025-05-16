package com.ivan.user_service.config

import com.ivan.user_service.kafka.SkillCommandEvent
import com.ivan.user_service.kafka.UserCommandEvent
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

    @Value("\${app.kafka.topics.user-commands}")
    private val userCommandsTopic: String,

    @Value("\${app.kafka.topics.skill-commands}")
    private val skillCommandsTopic: String
) {

    @Bean
    fun userCommandsTopic(): NewTopic {
        return TopicBuilder.name(userCommandsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun skillCommandsTopic(): NewTopic {
        return TopicBuilder.name(skillCommandsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun userCommandConsumerFactory(): ConsumerFactory<String, UserCommandEvent> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "user-service-group"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        props[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = UserCommandEvent::class.java.name
        props[JsonDeserializer.TRUSTED_PACKAGES] = "com.ivan.user_service.kafka"
        props[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun skillCommandConsumerFactory(): ConsumerFactory<String, SkillCommandEvent> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "user-service-group"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        props[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = SkillCommandEvent::class.java.name
        props[JsonDeserializer.TRUSTED_PACKAGES] = "com.ivan.user_service.kafka"
        props[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun userCommandKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserCommandEvent>  {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserCommandEvent>()
        factory.consumerFactory = userCommandConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }

    @Bean
    fun skillCommandKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, SkillCommandEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, SkillCommandEvent>()
        factory.consumerFactory = skillCommandConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }
}