package com.ivan.project_task_service.kafka

import com.ivan.project_task_service.util.SafeJsonDeserializer
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

    @Value("\${app.kafka.topics.project-task-results}")
    private val projectTasksResultTopic: String,

    @Value("\${app.kafka.topics.project-actions}")
    private val projectActionsTopic: String,

    @Value("\${app.kafka.topics.user-actions}")
    private val userActionsTopic: String,

    @Value("\${app.kafka.topics.skill-actions}")
    private val skillActionsTopic: String,

    @Value("\${app.kafka.topics.project-task-commands}")
    private val projectTasksCommandsTopic: String,

    @Value("\${app.kafka.topics.project-task-actions}")
    private val projectTasksActionsTopic: String,
) {

    @Bean
    fun userActionsTopic(): NewTopic {
        return TopicBuilder.name(userActionsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun skillActionsTopic(): NewTopic {
        return TopicBuilder.name(skillActionsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun projectTasksResultTopic(): NewTopic {
        return TopicBuilder.name(projectTasksResultTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun projectActionsTopic(): NewTopic {
        return TopicBuilder.name(projectActionsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun projectTasksCommandsTopic(): NewTopic {
        return TopicBuilder.name(projectTasksCommandsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun projectTasksActionsTopic(): NewTopic {
        return TopicBuilder.name(projectTasksActionsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun userActionsConsumerFactory(): ConsumerFactory<String, UserActionEvent> {
        return createConsumerFactory("project-task-group", UserActionEvent::class.java)
    }


    @Bean
    fun userActionsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserActionEvent> {
        return createListenerContainerFactory(userActionsConsumerFactory())
    }


    @Bean
    fun skillActionsConsumerFactory(): ConsumerFactory<String, SkillActionEvent> {
        return createConsumerFactory("project-task-group", SkillActionEvent::class.java)
    }


    @Bean
    fun skillActionsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, SkillActionEvent> {
        return createListenerContainerFactory(skillActionsConsumerFactory())
    }

    @Bean
    fun projectActionsConsumerFactory(): ConsumerFactory<String, ProjectActionEvent> {
        return createConsumerFactory("project-task-group", ProjectActionEvent::class.java)
    }


    @Bean
    fun projectActionsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ProjectActionEvent> {
        return createListenerContainerFactory(projectActionsConsumerFactory())
    }

    @Bean
    fun projectTaskCommandsActionsConsumerFactory(): ConsumerFactory<String, ProjectTaskCommandEvent> {
        return createConsumerFactory("project-task-group", ProjectTaskCommandEvent::class.java)
    }

    @Bean
    fun projectTaskCommandsListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ProjectTaskCommandEvent> {
        return createListenerContainerFactory(projectTaskCommandsActionsConsumerFactory())
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
            put(JsonDeserializer.TRUSTED_PACKAGES, "com.ivan.project_task_service.kafka")
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