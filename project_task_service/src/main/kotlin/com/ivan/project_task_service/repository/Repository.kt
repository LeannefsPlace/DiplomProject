package com.ivan.project_task_service.repository

import com.ivan.project_task_service.model.Branch
import com.ivan.project_task_service.model.Project
import com.ivan.project_task_service.model.Task
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProjectRepository : CoroutineCrudRepository<Project, Int> {
    suspend fun findProjectByProjectId(projectId: Int): Project?
    suspend fun findProjectsByProjectIdIsIn(projectIds: MutableCollection<Int>): List<Project>
    suspend fun deleteProjectByProjectId(projectId: Int): Boolean
}

@Repository
class TaskRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate
) {
    suspend fun findTasksWithContext(query: TaskFindDTO): List<TaskWithContextDto> {
        val criteria = Criteria().apply {
            query.taskId?.let { and("branches.tasks.taskId").`is`(it) }
            query.branchId?.let { and("branches.branchId").`is`(it) }
            query.projectId?.let { and("projectId").`is`(it) }
            query.assignedTo?.let { and("branches.tasks.assignedTo").`is`(it) }
            query.skillId?.let { and("branches.tasks.skillId").`is`(it) }
            query.done?.let { and("branches.tasks.done").`is`(it) }
            query.hasProblem?.let { and("branches.tasks.hasProblem").`is`(it) }
        }

        return mongoTemplate.find(Query(criteria), Project::class.java)
            .collectList()
            .awaitSingle()
            .flatMap { project ->
                project.branches.mapNotNull { branch ->
                    branch.tasks
                        .filter { task -> (query.branchId == null || branch.branchId == query.branchId) &&
                                (query.taskId == null || task.taskId == query.taskId) &&
                                (query.assignedTo == null || task.assignedTo == query.assignedTo) &&
                                (query.skillId == null || task.skillId == query.skillId) &&
                                (query.done == null || task.done == query.done) &&
                                (query.hasProblem == null || task.hasProblem == query.hasProblem) }
                        .map { task -> TaskWithContextDto(project.projectId, branch.branchId, task) }
                }.flatten()
            }
    }
}

data class ProjectFindDTO(
    val projectId: Int? = null,
    val active: Boolean? = null
)

data class TaskWithContextDto(
    val projectId: Int,
    val branchId: String,
    val task: Task
)

data class TaskFindDTO(
    val projectId: Int? = null,
    val branchId: String? = null,
    val taskId: String? = null,
    val parentId: String? = null,
    val assignedTo: Int? = null,
    val skillId: Int? = null,
    val done: Boolean? = null,
    val hasProblem: Boolean? = null,
    val startDateFrom: LocalDate? = null,
    val startDateTo: LocalDate? = null,
    val endDateFrom: LocalDate? = null,
    val endDateTo: LocalDate? = null
)