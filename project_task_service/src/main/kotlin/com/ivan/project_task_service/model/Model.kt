package com.ivan.project_task_service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.UUID

@Document(collection = "projects")
data class Project(
    @Indexed(unique = true)
    @Id
    val projectId: Int, //not null

    var branches: List<Branch> = emptyList(), //not editable

    var statistics: TaskStatistics //not null
)

data class Branch(
    @Indexed
    val branchId: String = UUID.randomUUID().toString(), //not null

    val name: String, //not null

    val active: Boolean = true, //not null

    @Indexed
    var tasks: List<Task> = emptyList(), //not editable

    var statistics: TaskStatistics //not null
)

data class Task(
    @Indexed
    val taskId: String = UUID.randomUUID().toString(), //not null

    @Indexed
    val parentId: String?, //nullable!!!

    val title: String, //not null

    val description: String, //not null

    val startDate: LocalDate, //not null

    val endDate: LocalDate, //not null

    val done: Boolean = false, //not null

    val hasProblem: Boolean = false, //not null

    val problemMessage: String? = null, //nullable!!!

    @Indexed(sparse = true)
    val skillId: Int?, //nullable!!!

    @Indexed(sparse = true)
    val assignedTo: Int?, //nullable!!!

    val file: String?, //nullable!!!
)

data class TaskStatistics(
    var taskCount: Int, //not null
    var completedTasksCount: Int, //not null
    var delayedTasksCount: Int, //not null
    var problemTasksCount: Int //not null
)