package com.ivan.gateway_service.kafka

import com.ivan.gateway_service.util.SafeJsonDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,

    // Топики
    @Value("\${app.kafka.topics.project-actions}")
    private val projectActionsTopic: String,

    @Value("\${app.kafka.topics.project-task-commands}")
    private val projectTasksCommandsTopic: String,

    @Value("\${app.kafka.topics.project-task-results}")
    private val projectTasksResultTopic: String,

    @Value("\${app.kafka.topics.project-task-actions}")
    private val projectTasksActionsTopic: String,

    @Value("\${app.kafka.topics.user-actions}")
    private val userActionsTopic: String,

    @Value("\${app.kafka.topics.skill-actions}")
    private val skillActionsTopic: String,

    @Value("\${app.kafka.topics.project-commands}")
    private val projectCommandsTopic: String,

    @Value("\${app.kafka.topics.project-results}")
    private val projectResultsTopic: String,

    @Value("\${app.kafka.topics.backup-commands}")
    private val backupCommandsTopic: String,

    @Value("\${app.kafka.topics.backup-results}")
    private val backupResultsTopic: String,

    @Value("\${app.kafka.topics.user-commands}")
    private val userCommandsTopic: String,

    @Value("\${app.kafka.topics.user-results}")
    private val userResultsTopic: String,

    @Value("\${app.kafka.topics.session-commands}")
    private val sessionCommandsTopic: String,

    @Value("\${app.kafka.topics.session-results}")
    private val sessionResultsTopic: String,

    @Value("\${app.kafka.topics.session-actions}")
    private val sessionActionsTopic: String,

    @Value("\${app.kafka.topics.skill-commands}")
    private val skillCommandsTopic: String,

    @Value("\${app.kafka.topics.skill-results}")
    private val skillResultsTopic: String
) {

    @Bean
    fun projectActionsTopic() = TopicBuilder.name(projectActionsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun projectTaskCommandsTopic() = TopicBuilder.name(projectTasksCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun projectTaskResultsTopic() = TopicBuilder.name(projectTasksResultTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun projectTaskActionsTopic() = TopicBuilder.name(projectTasksActionsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun userActionsTopic() = TopicBuilder.name(userActionsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun skillActionsTopic() = TopicBuilder.name(skillActionsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun projectCommandsTopic() = TopicBuilder.name(projectCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun projectResultsTopic() = TopicBuilder.name(projectResultsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun backupCommandsTopic() = TopicBuilder.name(backupCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun backupResultsTopic() = TopicBuilder.name(backupResultsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun userCommandsTopic() = TopicBuilder.name(userCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun userResultsTopic() = TopicBuilder.name(userResultsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun sessionCommandsTopic() = TopicBuilder.name(sessionCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun sessionResultsTopic() = TopicBuilder.name(sessionResultsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun sessionActionsTopic() = TopicBuilder.name(sessionActionsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun skillCommandsTopic() = TopicBuilder.name(skillCommandsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun skillResultsTopic() = TopicBuilder.name(skillResultsTopic)
        .partitions(3)
        .replicas(1)
        .build()

    // Project Actions
    @Bean
    fun projectActionsConsumerFactory() =
        createConsumerFactory("gateway-group", ProjectActionEvent::class.java)

    @Bean
    fun projectActionsListenerContainerFactory() =
        createListenerContainerFactory(projectActionsConsumerFactory())

    // Project Task Results
    @Bean
    fun projectTaskResultsConsumerFactory() =
        createConsumerFactory("gateway-group", ProjectTaskResultEvent::class.java)

    @Bean
    fun projectTaskResultsListenerContainerFactory() =
        createListenerContainerFactory(projectTaskResultsConsumerFactory())

    // Project Task Actions
    @Bean
    fun projectTaskActionsConsumerFactory() =
        createConsumerFactory("gateway-group", ProjectTaskActionEvent::class.java)

    @Bean
    fun projectTaskActionsListenerContainerFactory() =
        createListenerContainerFactory(projectTaskActionsConsumerFactory())

    // User Actions
    @Bean
    fun userActionsConsumerFactory() =
        createConsumerFactory("gateway-group", UserActionEvent::class.java)

    @Bean
    fun userActionsListenerContainerFactory() =
        createListenerContainerFactory(userActionsConsumerFactory())

    // Skill Actions
    @Bean
    fun skillActionsConsumerFactory() =
        createConsumerFactory("gateway-group", SkillActionEvent::class.java)

    @Bean
    fun skillActionsListenerContainerFactory() =
        createListenerContainerFactory(skillActionsConsumerFactory())

    // Project Results
    @Bean
    fun projectResultsConsumerFactory() =
        createConsumerFactory("gateway-group", ProjectResultEvent::class.java)

    @Bean
    fun projectResultsListenerContainerFactory() =
        createListenerContainerFactory(projectResultsConsumerFactory())

    // Backup Results
    @Bean
    fun backupResultsConsumerFactory() =
        createConsumerFactory("gateway-group", BackupResultEvent::class.java)

    @Bean
    fun backupResultsListenerContainerFactory() =
        createListenerContainerFactory(backupResultsConsumerFactory())

    // User Results
    @Bean
    fun userResultsConsumerFactory() =
        createConsumerFactory("gateway-group", UserResultEvent::class.java)

    @Bean
    fun userResultsListenerContainerFactory() =
        createListenerContainerFactory(userResultsConsumerFactory())

    // Session Results
    @Bean
    fun sessionResultsConsumerFactory() =
        createConsumerFactory("gateway-group", SessionResultEvent::class.java)

    @Bean
    fun sessionResultsListenerContainerFactory() =
        createListenerContainerFactory(sessionResultsConsumerFactory())

    // Session Actions
    @Bean
    fun sessionActionsConsumerFactory() =
        createConsumerFactory("gateway-group", SessionActionEvent::class.java)

    @Bean
    fun sessionActionsListenerContainerFactory() =
        createListenerContainerFactory(sessionActionsConsumerFactory())

    // Skill Results
    @Bean
    fun skillResultsConsumerFactory() =
        createConsumerFactory("gateway-group", SkillResultEvent::class.java)

    @Bean
    fun skillResultsListenerContainerFactory() =
        createListenerContainerFactory(skillResultsConsumerFactory())

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
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to "60000"
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to "20000"
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to "300000"
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