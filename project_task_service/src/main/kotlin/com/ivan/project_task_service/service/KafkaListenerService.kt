package com.ivan.project_task_service.service

import com.ivan.project_task_service.kafka.*
import com.ivan.project_task_service.model.Project
import com.ivan.project_task_service.model.TaskStatistics
import com.ivan.project_task_service.repository.ProjectRepository
import com.ivan.project_task_service.repository.TaskFindDTO
import org.apache.kafka.common.requests.DeleteAclsResponse.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class KafkaListenerService(
    private val projectTaskService: ProjectTaskService,
    private val kafkaProducerService: KafkaProducerService,
    private val projectRepository: ProjectRepository
) {
    @KafkaListener(
        topics = ["\${app.kafka.topics.project-task-commands}"],
        containerFactory = "projectTaskCommandsListenerContainerFactory"
    )
    suspend fun handleTaskCommands(command: ProjectTaskCommandEvent, ack: Acknowledgment) {
            try {
                when (command.commandType) {
                    ProjectTaskEventType.GET_PROJECT -> getProject(command)
                    ProjectTaskEventType.TASKS_FOR_USER -> getTaskForUser(command)
                    ProjectTaskEventType.CREATE_TASK -> createTask(command)
                    ProjectTaskEventType.UPDATE_TASK -> updateTask(command)
                    ProjectTaskEventType.DELETE_TASK -> deleteTask(command)
                    ProjectTaskEventType.CREATE_BRANCH -> createBranch(command)
                    ProjectTaskEventType.DELETE_BRANCH -> deleteBranch(command)
                    ProjectTaskEventType.UPDATE_BRANCH -> updateBranch(command)
                }
                ack.acknowledge()
            } catch (e: Exception) {
                log.error("Error processing project task command: ${e.message}", e)
                ack.acknowledge()
            }
    }

    suspend fun getProject(command: ProjectTaskCommandEvent) {
        try{
            requireNotNull(command.projectId)
            val project = projectTaskService.getProject(command.projectId)?:throw IllegalStateException("Project details not found")
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(projectTaskService.saveProject(
                        Project(
                            projectId = command.projectId?:throw IllegalStateException("Project id not found"),
                            branches = emptyList(),
                            statistics = TaskStatistics(
                                0, 0, 0 ,0
                            )
                        )
                    )),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun getTaskForUser(command: ProjectTaskCommandEvent) {
        try{
            val tasks = projectTaskService.findTasks(TaskFindDTO(
                assignedTo = requireNotNull(command.userId)
            ))
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = emptyList(),
                    tasks = tasks
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun createTask(command: ProjectTaskCommandEvent) {
        try{
            val task = requireNotNull(command.task).toTask()

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
                project.branches.map { b ->
                    if (b.branchId == requireNotNull(command.branchId)) b.copy(tasks = b.tasks.plus(task)) else b
                }

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject( project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun updateTask(command: ProjectTaskCommandEvent) {
        try{
            val task = requireNotNull(command.task).toTask()

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
                project.branches.map {
                    b ->
                    if (b.branchId == requireNotNull(command.branchId))
                        b.copy(tasks = b.tasks.map{ t ->
                            if
                                (task.taskId == t.taskId) task
                            else t})
                    else b }

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject(project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun deleteTask(command: ProjectTaskCommandEvent) {
        try{
            val task = requireNotNull(command.task)

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
            project.branches.map {
                b -> if(b.branchId == requireNotNull(command.branchId)) b.copy(tasks = b.tasks.filter { t -> t.taskId != task.taskId }) else b
            }

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject(project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun createBranch(command: ProjectTaskCommandEvent) {
        try{
            val branch = requireNotNull(command.branch).toBranch()

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
                project.branches.plus(branch)

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject(project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun updateBranch(command: ProjectTaskCommandEvent) {
        try{
            val branch = requireNotNull(command.branch)

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
                project.branches.map {
                b -> if (b.branchId == branch.branchId) b.copy(name = branch.name?:b.name, active = branch.active) else b
            }

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject(project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }

    suspend fun deleteBranch(command: ProjectTaskCommandEvent) {
        try{
            val branch = requireNotNull(command.branch)

            val project = requireNotNull(projectTaskService.getProject(requireNotNull(command.projectId)))

            project.branches =
                project.branches.filterNot {
                    b -> b.branchId == branch.branchId
            }

            updateProjectDetailsStatistics(project)

            projectTaskService.saveProject(project)

            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = true,
                    errorMessage = null,
                    projects = listOf(project),
                    tasks = emptyList()
                )
            )

            kafkaProducerService.sendProjectTaskAction(
                ProjectTaskActionEvent(
                    eventId = command.eventId,
                    projectId = project.projectId
                )
            )
        }catch(e:Exception){
            log.error("Error processing project task command: ${e.message}", e)
            kafkaProducerService.sendProjectTaskResult(
                ProjectTaskResultEvent(
                    eventId = command.eventId,
                    success = false,
                    errorMessage = "Error processing project task command: ${e.message}",
                    projects = emptyList(),
                    tasks = emptyList()
                )
            )
        }
    }


    @KafkaListener(
        topics = ["\${app.kafka.topics.user-actions}"],
        containerFactory = "userActionsListenerContainerFactory"
    )
    suspend fun handleUserActions(command: UserActionEvent, ack: Acknowledgment) {
        try {
            if (command.actionType == "USER_DELETED") {
                val tasks = projectTaskService.findTasks(TaskFindDTO(
                    assignedTo = command.userId
                ))
                for (task in tasks) {
                    val project = requireNotNull(projectTaskService.getProject(requireNotNull(task.projectId)))

                    project.branches = project.branches.map {
                        b -> if (b.branchId == task.branchId)
                            b.copy(tasks =
                                b.tasks.map { t ->
                                    if (t.assignedTo == command.userId) t.copy(assignedTo = null) else t
                                }
                            ) else b
                    }

                    projectTaskService.saveProject(project)
                }
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing user action: ${e.message}", e)
            ack.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.skill-actions}"],
        containerFactory = "skillActionsListenerContainerFactory"
    )
    suspend fun handleSkillActions(command: SkillActionEvent, ack: Acknowledgment) {
        try {
            if (command.actionType == "SKILL_DELETED") {
                val tasks = projectTaskService.findTasks(TaskFindDTO(
                    skillId = command.skillId
                ))
                for (task in tasks) {
                    val project = requireNotNull(projectTaskService.getProject(requireNotNull(task.projectId)))

                    project.branches = project.branches.map {
                            b -> if (b.branchId == task.branchId)
                        b.copy(tasks =
                            b.tasks.map { t ->
                                if (t.skillId == command.skillId) t.copy(skillId = null) else t
                            }
                        ) else b
                    }

                    projectTaskService.saveProject(project)
                }
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing skill action: ${e.message}", e)
            ack.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.project-actions}"],
        containerFactory = "projectActionsListenerContainerFactory"
    )
    suspend fun handleProjectActions(command: ProjectActionEvent, ack: Acknowledgment) {
        try {
            when (command.actionType) {
                ProjectActionType.CREATE -> createProjectDetails(command)
                ProjectActionType.DELETE -> deleteProjectDetails(command)
                ProjectActionType.UPDATE -> {}
                ProjectActionType.DISCHARGE -> {}
                ProjectActionType.EDIT_ROLE -> {}
            }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing project action: ${e.message}", e)
            ack.acknowledge()
        }
    }

    suspend fun createProjectDetails(command: ProjectActionEvent) {
        println("created Details for: ${command.projectId}")

        projectTaskService.saveProject(
            Project(
                projectId = requireNotNull(command.projectId),
                branches = emptyList(),
                statistics = TaskStatistics(
                    taskCount = 0,
                    completedTasksCount = 0,
                    delayedTasksCount = 0,
                    problemTasksCount = 0
                )
            )
        )

        kafkaProducerService.sendProjectTaskAction(
            ProjectTaskActionEvent(
                eventId = command.eventId,
                projectId = command.projectId
            )
        )
    }

    suspend fun deleteProjectDetails(command: ProjectActionEvent) {
        projectTaskService.deleteProject(command.projectId)

        kafkaProducerService.sendProjectTaskAction(
            ProjectTaskActionEvent(
                eventId = command.eventId,
                projectId = command.projectId
            )
        )
    }

    suspend fun updateProjectDetailsStatistics(project: Project) : Project {
        val projectStatistics = TaskStatistics(
            taskCount = 0,
            completedTasksCount = 0,
            delayedTasksCount = 0,
            problemTasksCount = 0
        )

        for(branch in project.branches){
            val branchStatistics = TaskStatistics(
                taskCount = 0,
                completedTasksCount = 0,
                delayedTasksCount = 0,
                problemTasksCount = 0
            )

            for (task in branch.tasks) {
                branchStatistics.taskCount++
                if (task.done) branchStatistics.completedTasksCount++
                if (task.hasProblem) branchStatistics.problemTasksCount++
                if (task.endDate < LocalDate.now()) branchStatistics.delayedTasksCount++
            }

            branch.statistics = branchStatistics
            projectStatistics.apply {
                taskCount += branchStatistics.taskCount
                completedTasksCount += branchStatistics.completedTasksCount
                problemTasksCount += branchStatistics.problemTasksCount
                delayedTasksCount += branchStatistics.delayedTasksCount
            }
        }

        project.statistics = projectStatistics

        return project
    }
}