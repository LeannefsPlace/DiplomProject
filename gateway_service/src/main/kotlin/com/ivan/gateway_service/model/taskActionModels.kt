package com.ivan.gateway_service.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.ivan.gateway_service.kafka.TaskStatistics
import java.time.LocalDate
import java.util.*

data class Branch(
    @JsonProperty val branchId: String = UUID.randomUUID().toString(),

    @JsonProperty val name: String,

    @JsonProperty val active: Boolean = true,

    @JsonProperty var tasks: List<Task> = emptyList(),

    @JsonProperty var statistics: TaskStatistics
)

data class Task(
    @JsonProperty val taskId: String = UUID.randomUUID().toString(),

    @JsonProperty val parentId: String?,

    @JsonProperty val title: String,

    @JsonProperty val description: String,

    @JsonProperty val startDate: LocalDate,

    @JsonProperty val endDate: LocalDate,

    @JsonProperty val done: Boolean = false,

    @JsonProperty val hasProblem: Boolean = false,

    @JsonProperty val problemMessage: String? = null,

    @JsonProperty val skillId: Int?,

    @JsonProperty val assignedTo: Int?,

    @JsonProperty val file: String?,
)

data class Project(
    @JsonProperty val projectId: Int,

    @JsonProperty var branches: List<Branch> = emptyList(),

    @JsonProperty var statistics: TaskStatistics
)
