package com.ivan.gateway_service.model

import com.ivan.gateway_service.kafka.TaskStatistics
import java.time.LocalDate
import java.util.*

data class Branch(
    val branchId: String = UUID.randomUUID().toString(),

    val name: String,

    val active: Boolean = true,

    var tasks: List<Task> = emptyList(),

    var statistics: TaskStatistics
)

data class Task(
    val taskId: String = UUID.randomUUID().toString(),

    val parentId: String?,

    val title: String,

    val description: String,

    val startDate: LocalDate,

    val endDate: LocalDate,

    val done: Boolean = false,

    val hasProblem: Boolean = false,

    val problemMessage: String? = null,

    val skillId: Int?,

    val assignedTo: Int?,

    val file: String?,
)

data class Project(
    val projectId: Int,

    var branches: List<Branch> = emptyList(),

    var statistics: TaskStatistics
)
