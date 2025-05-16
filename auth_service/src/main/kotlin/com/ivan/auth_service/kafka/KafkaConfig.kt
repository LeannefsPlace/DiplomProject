package com.ivan.auth_service.kafka

import com.ivan.auth_service.util.SafeJsonDeserializer
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

    @Value("\${app.kafka.topics.user-results}")
    private val userResultsTopic: String,

    @Value("\${app.kafka.topics.user-actions}")
    private val userActionsTopic: String,

    @Value("\${app.kafka.topics.session-commands}")
    private val sessionCommandsTopic: String,
) {

    // Топики
    @Bean
    fun userResultsTopic(): NewTopic {
        return TopicBuilder.name(userResultsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun userActionsTopic(): NewTopic {
        return TopicBuilder.name(userActionsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun sessionCommandsTopic(): NewTopic {
        return TopicBuilder.name(sessionCommandsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun userResultsConsumerFactory(): ConsumerFactory<String, UserResultEvent> {
        return createConsumerFactory("user-results-group", UserResultEvent::class.java)
    }

    @Bean
    fun userActionsConsumerFactory(): ConsumerFactory<String, UserActionEvent> {
        return createConsumerFactory("user-actions-group", UserActionEvent::class.java)
    }

    @Bean
    fun sessionCommandsConsumerFactory(): ConsumerFactory<String, SessionCommandEvent> {
        return createConsumerFactory("session-commands-group", SessionCommandEvent::class.java)
    }

    @Bean
    fun userResultsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserResultEvent> {
        return createListenerContainerFactory(userResultsConsumerFactory())
    }

    @Bean
    fun userActionsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserActionEvent> {
        return createListenerContainerFactory(userActionsConsumerFactory())
    }


    @Bean
    fun sessionCommandsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, SessionCommandEvent> {
        return createListenerContainerFactory(sessionCommandsConsumerFactory())
    }

    private fun <T> createConsumerFactory(groupId: String, valueType: Class<T>): ConsumerFactory<String, T> {
        val props = HashMap<String, Any>().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer::class.java)
            put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer::class.java)
            put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, SafeJsonDeserializer::class.java)
            put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.name)
            put(JsonDeserializer.TRUSTED_PACKAGES, "com.ivan.auth_service.kafka")
            put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false)
        }
        return DefaultKafkaConsumerFactory(props)
    }

    private fun <T> createListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, T>
    ): ConcurrentKafkaListenerContainerFactory<String, T> {
        return ConcurrentKafkaListenerContainerFactory<String, T>().apply {
            this.consumerFactory = consumerFactory
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        }
    }
}