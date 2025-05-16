package com.ivan.project_task_service.service

import com.ivan.project_task_service.model.Project
import com.ivan.project_task_service.repository.ProjectRepository
import com.ivan.project_task_service.repository.TaskFindDTO
import com.ivan.project_task_service.repository.TaskRepositoryImpl
import com.ivan.project_task_service.repository.TaskWithContextDto
import org.springframework.stereotype.Service

@Service
class ProjectTaskService(
    private val projectRepository: ProjectRepository,
    private val taskRepositoryImpl: TaskRepositoryImpl
) {
    // Проекты
    suspend fun saveProject(project: Project): Project =
        projectRepository.save(project)

    suspend fun getProject(projectId: Int): Project? =
        projectRepository.findProjectByProjectId(projectId)

    suspend fun deleteProject(projectId: Int): Boolean =
        projectRepository.deleteProjectByProjectId(projectId)

    // Задачи
    suspend fun findTasks(query: TaskFindDTO): List<TaskWithContextDto> =
        taskRepositoryImpl.findTasksWithContext(query)
}